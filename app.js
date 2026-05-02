"use strict";

const ZIP = {
  LFH: 0x04034b50,
  CEN: 0x02014b50,
  EOCD: 0x06054b50,
  DD: 0x08074b50
};

const els = {
  fileInput: document.querySelector("#fileInput"),
  dropZone: document.querySelector("#dropZone"),
  extractPanel: document.querySelector("#extractPanel"),
  browserPanel: document.querySelector("#browserPanel"),
  stateText: document.querySelector("#stateText"),
  meterFill: document.querySelector("#meterFill"),
  browserSummary: document.querySelector("#browserSummary"),
  fileFilter: document.querySelector("#fileFilter"),
  fileList: document.querySelector("#fileList"),
  previewEmpty: document.querySelector("#previewEmpty"),
  previewContent: document.querySelector("#previewContent"),
  downloadFileButton: document.querySelector("#downloadFileButton"),
  downloadZipButton: document.querySelector("#downloadZipButton"),
  closeBrowserButton: document.querySelector("#closeBrowserButton")
};

const textDecoder = new TextDecoder();
const utf8Decoder = new TextDecoder("utf-8", { fatal: false });
const fallbackNameDecoder = new TextDecoder("windows-1252", { fatal: false });
const textEncoder = new TextEncoder();

const crcTable = buildCrcTable();

const state = {
  entries: [],
  tree: null,
  expandedDirs: new Set(),
  activeEntry: null,
  outputName: "",
  previewUrl: "",
  progress: 0,
  repairs: { png: 0 }
};

bindEvents();

function bindEvents() {
  els.fileInput.addEventListener("change", () => {
    const file = els.fileInput.files && els.fileInput.files[0];
    if (file) void extractFile(file);
  });

  els.fileFilter.addEventListener("input", () => renderFileList());
  els.downloadFileButton.addEventListener("click", () => {
    if (state.activeEntry) downloadEntry(state.activeEntry);
  });
  els.downloadZipButton.addEventListener("click", downloadRecoveredZip);
  els.closeBrowserButton.addEventListener("click", resetToExtractor);

  els.dropZone.addEventListener("dragenter", showDragState);
  els.dropZone.addEventListener("dragover", showDragState);
  els.dropZone.addEventListener("dragleave", (event) => {
    if (!els.dropZone.contains(event.relatedTarget)) {
      els.dropZone.classList.remove("is-dragging");
    }
  });
  els.dropZone.addEventListener("drop", (event) => {
    event.preventDefault();
    els.dropZone.classList.remove("is-dragging");

    const file = event.dataTransfer.files && event.dataTransfer.files[0];
    if (file) void extractFile(file);
  });
}

function showDragState(event) {
  event.preventDefault();
  els.dropZone.classList.add("is-dragging");
}

async function extractFile(file) {
  setBusy(true);
  resetExtractionState();
  setProgress(2);
  setStatus(`Loaded ${file.name} (${formatBytes(file.size)})`);

  try {
    const bytes = new Uint8Array(await file.arrayBuffer());
    assertZipMagic(bytes);

    setProgress(12);
    setStatus("Reading ZIP structure");

    const entries = await readRecoverableEntries(bytes, (done, total, message) => {
      setProgress(12 + Math.round((done / Math.max(total, 1)) * 58));
      if (message) setStatus(message);
    });

    if (!entries.some((entry) => entry.name === "pack.mcmeta")) {
      throw new Error("This ZIP does not contain a root pack.mcmeta file.");
    }

    state.entries = entries.sort((a, b) => a.name.localeCompare(b.name));
    state.tree = buildFileTree(state.entries);
    state.outputName = recoveredZipName(file.name);

    setProgress(100);
    setStatus(`Recovered ${state.entries.length} file${state.entries.length === 1 ? "" : "s"}`);
    showBrowser();
  } catch (error) {
    setProgress(0);
    setStatus(error instanceof Error ? error.message : String(error));
  } finally {
    setBusy(false);
    els.fileInput.value = "";
  }
}

function resetExtractionState() {
  closePreviewUrl();
  state.entries = [];
  state.tree = null;
  state.expandedDirs = new Set();
  state.activeEntry = null;
  state.outputName = "";
  state.repairs = { png: 0 };
  els.downloadFileButton.disabled = true;
}

function showBrowser() {
  els.extractPanel.hidden = true;
  els.browserPanel.hidden = false;
  els.fileFilter.value = "";
  els.browserSummary.textContent = `${state.entries.length} files recovered${state.repairs.png ? `, ${state.repairs.png} PNG files repaired` : ""}`;
  renderFileList();
  clearPreview();
}

function resetToExtractor() {
  resetExtractionState();
  els.fileList.textContent = "";
  els.browserPanel.hidden = true;
  els.extractPanel.hidden = false;
  setProgress(0);
  setStatus("Waiting for a ZIP file");
}

function renderFileList() {
  const fragment = document.createDocumentFragment();
  const query = els.fileFilter.value.trim().toLowerCase();

  fragment.append(createHeaderRow());

  if (query) {
    renderSearchResults(fragment, query);
  } else if (state.tree) {
    renderDirectory(state.tree, fragment, 0);
  }

  els.fileList.textContent = "";
  els.fileList.append(fragment);
}

function renderSearchResults(fragment, query) {
  const matches = state.entries.filter((entry) => entry.name.toLowerCase().includes(query));
  const visibleMatches = matches.slice(0, 1200);

  for (const entry of visibleMatches) {
    fragment.append(createFileRow(entry, 0));
  }

  if (matches.length > visibleMatches.length) {
    fragment.append(createMessageRow(`${matches.length - visibleMatches.length} more matches hidden. Keep typing to narrow results.`));
  }
}

function renderDirectory(directory, fragment, depth) {
  for (const child of directory.dirs) {
    fragment.append(createDirectoryRow(child, depth));
    if (state.expandedDirs.has(child.path)) {
      renderDirectory(child, fragment, depth + 1);
    }
  }

  for (const file of directory.files) {
    fragment.append(createFileRow(file, depth));
  }
}

function buildFileTree(entries) {
  const root = createDirectoryNode("", "");

  for (const entry of entries) {
    const parts = entry.name.split("/");
    const fileName = parts.pop() || entry.name;
    let node = root;
    let path = "";

    for (const part of parts) {
      path = path ? `${path}/${part}` : part;
      let next = node.dirMap.get(part);

      if (!next) {
        next = createDirectoryNode(part, path);
        node.dirMap.set(part, next);
        node.dirs.push(next);
      }

      node = next;
    }

    node.files.push({ ...entry, displayName: fileName });
  }

  sortDirectory(root);
  return root;
}

function createDirectoryNode(name, path) {
  return { name, path, dirs: [], dirMap: new Map(), files: [] };
}

function sortDirectory(directory) {
  directory.dirs.sort((a, b) => a.name.localeCompare(b.name));
  directory.files.sort((a, b) => a.displayName.localeCompare(b.displayName));

  for (const child of directory.dirs) {
    sortDirectory(child);
    delete child.dirMap;
  }
}

function createHeaderRow() {
  const row = document.createElement("div");
  row.className = "file-row file-header-row";
  row.append(createCell("Name"), createCell("Type"), createCell("Size"));
  return row;
}

function createDirectoryRow(directory, depth) {
  const row = createRow("button", "file-row file-directory");
  const isOpen = state.expandedDirs.has(directory.path);

  row.title = directory.path;
  row.addEventListener("click", () => {
    if (isOpen) {
      state.expandedDirs.delete(directory.path);
    } else {
      state.expandedDirs.add(directory.path);
    }
    renderFileList();
  });

  row.append(createDirectoryNameCell(directory, depth, isOpen), createCell(""), createCell(""));
  return row;
}

function createFileRow(entry, depth) {
  const type = previewType(entry.name);
  const active = entry.name === (state.activeEntry && state.activeEntry.name);
  const row = createRow("button", `file-row file-file file-type-${type}${active ? " is-active" : ""}`);

  row.title = entry.name;
  row.setAttribute("role", "option");
  row.setAttribute("aria-selected", active ? "true" : "false");
  row.addEventListener("click", () => openEntry(entry));
  row.addEventListener("contextmenu", (event) => {
    event.preventDefault();
    if (window.confirm(`Download ${entry.name}?`)) downloadEntry(entry);
  });

  row.append(
    createNameCell(entry.displayName || entry.name, depth),
    createCell(fileExtension(entry.name)),
    createCell(formatBytes(entry.data.length))
  );
  return row;
}

function createRow(tag, className) {
  const row = document.createElement(tag);
  row.className = className;
  if (tag === "button") row.type = "button";
  return row;
}

function createDirectoryNameCell(directory, depth, isOpen) {
  const cell = createNameCell("", depth);
  const disclosure = document.createElement("span");
  const label = document.createElement("span");

  disclosure.className = "disclosure";
  disclosure.textContent = isOpen ? "⌄" : "›";
  label.textContent = directory.name;

  cell.append(disclosure, label);
  return cell;
}

function createNameCell(text, depth) {
  const cell = createCell(text);
  cell.className = "file-name";
  cell.style.paddingLeft = `${8 + depth * 18}px`;
  return cell;
}

function createCell(text) {
  const cell = document.createElement("span");
  cell.textContent = text;
  return cell;
}

function createMessageRow(text) {
  const row = document.createElement("div");
  row.className = "file-row file-message";
  row.textContent = text;
  return row;
}

function openEntry(entry) {
  state.activeEntry = entry;
  els.downloadFileButton.disabled = false;
  renderFileList();
  clearPreview();

  const type = previewType(entry.name);
  const meta = createPreviewMeta(entry);

  els.previewEmpty.hidden = true;
  els.previewEmpty.textContent = "";
  els.previewContent.hidden = false;
  els.previewContent.append(meta);

  if (type === "image") {
    previewImage(entry);
  } else if (type === "audio") {
    previewAudio(entry);
  } else if (type === "json" || type === "text") {
    previewText(entry, type);
  } else {
    showUnsupportedPreview();
  }
}

function createPreviewMeta(entry) {
  const meta = document.createElement("p");
  meta.className = "preview-meta";
  meta.textContent = `${entry.name} · ${formatBytes(entry.data.length)}`;
  return meta;
}

function previewImage(entry) {
  state.previewUrl = URL.createObjectURL(new Blob([entry.data], { type: imageMime(entry.name) }));

  const image = document.createElement("img");
  image.src = state.previewUrl;
  image.alt = entry.name;
  els.previewContent.append(image);
}

function previewAudio(entry) {
  state.previewUrl = URL.createObjectURL(new Blob([entry.data], { type: "audio/ogg" }));

  const audio = document.createElement("audio");
  audio.controls = true;
  audio.src = state.previewUrl;
  els.previewContent.append(audio);
}

function previewText(entry, type) {
  const pre = document.createElement("pre");
  pre.className = "preview-text";
  pre.textContent = type === "json" ? prettifyJson(entry.data) : textDecoder.decode(entry.data);
  els.previewContent.append(pre);
}

function showUnsupportedPreview() {
  els.previewContent.hidden = true;
  els.previewContent.textContent = "";
  els.previewEmpty.hidden = false;
  els.previewEmpty.textContent = "Preview is not available for this file type.";
}

function clearPreview() {
  closePreviewUrl();
  els.previewContent.textContent = "";
  els.previewContent.hidden = true;
  els.previewEmpty.hidden = false;
  els.previewEmpty.textContent = "Select a supported file to preview it.";
}

function closePreviewUrl() {
  if (!state.previewUrl) return;
  URL.revokeObjectURL(state.previewUrl);
  state.previewUrl = "";
}

function downloadEntry(entry) {
  const fileName = entry.name.split("/").pop() || "file";
  downloadBlob(new Blob([entry.data], { type: downloadMime(entry.name) }), fileName);
}

function downloadRecoveredZip() {
  if (!state.entries.length) return;

  els.downloadZipButton.disabled = true;
  els.downloadZipButton.textContent = "Preparing ZIP";

  try {
    const zip = buildStoredZip(state.entries);
    downloadBlob(new Blob([zip], { type: "application/zip" }), state.outputName || "resource-pack-recovered.zip");
  } finally {
    els.downloadZipButton.disabled = false;
    els.downloadZipButton.textContent = "Download ZIP";
  }
}

async function readRecoverableEntries(zip, onProgress) {
  const centralEntries = readCentralEntries(zip);
  if (centralEntries.length) {
    const entries = await readEntriesFromCentralDirectory(zip, centralEntries, onProgress);
    if (entries.length) return entries;
  }

  const entries = await readEntriesFromLocalHeaders(zip, onProgress);
  if (!entries.length) throw new Error("No readable files were found in the ZIP local headers.");
  return entries;
}

async function readEntriesFromLocalHeaders(zip, onProgress) {
  const view = viewOf(zip);
  const entries = [];
  let offset = 0;

  while (offset + 4 <= zip.length) {
    let signature = get32(view, offset);

    if (signature === ZIP.CEN || signature === ZIP.EOCD) break;
    if (signature !== ZIP.LFH) {
      const next = findSignature(view, offset + 1, ZIP.LFH);
      if (next < 0) break;
      offset = next;
      signature = ZIP.LFH;
    }

    if (offset + 30 > zip.length) break;

    const flags = get16(view, offset + 6);
    const method = get16(view, offset + 8);
    const compressedSize = get32(view, offset + 18);
    const fileNameLength = get16(view, offset + 26);
    const extraLength = get16(view, offset + 28);
    const nameStart = offset + 30;
    const dataStart = nameStart + fileNameLength + extraLength;

    if (dataStart > zip.length) break;

    const name = normalizeZipName(decodeZipName(zip.subarray(nameStart, nameStart + fileNameLength), flags));
    const encrypted = (flags & 1) !== 0;
    const usesDescriptor = (flags & 8) !== 0;
    const nextRecord = findNextRecord(view, dataStart);
    offset = nextRecord > dataStart ? nextRecord : zip.length;

    if (!name || name.endsWith("/") || encrypted || !isSupportedZipMethod(method)) continue;

    const data = await decodeLocalPayload(zip, view, dataStart, compressedSize, method, usesDescriptor, nextRecord);
    if (!data) continue;

    entries.push({ name, data: repairPayload(name, data) });
    onProgress(entries.length, entries.length + Math.max(1, Math.floor((zip.length - offset) / 65536)), `Extracting ${name}`);
  }

  return entries;
}

async function decodeLocalPayload(zip, view, dataStart, compressedSize, method, usesDescriptor, nextRecord) {
  for (const payload of payloadCandidates(zip, view, dataStart, compressedSize, usesDescriptor, nextRecord)) {
    try {
      return method === 0 ? payload : await inflateRaw(payload);
    } catch {
      // Try the next candidate. Corrupt ZIPs often have ambiguous data descriptors.
    }
  }
  return null;
}

function readCentralEntries(zip) {
  const view = viewOf(zip);
  const entries = [];
  let offset = 0;

  while (offset + 46 <= zip.length) {
    const next = findSignature(view, offset, ZIP.CEN);
    if (next < 0) break;
    offset = next + 1;

    const nameLength = get16(view, next + 28);
    const extraLength = get16(view, next + 30);
    const commentLength = get16(view, next + 32);
    const nameStart = next + 46;
    const nextEntry = nameStart + nameLength + extraLength + commentLength;

    if (nextEntry > zip.length) continue;

    const flags = get16(view, next + 8);
    const name = normalizeZipName(decodeZipName(zip.subarray(nameStart, nameStart + nameLength), flags));
    if (!name) continue;

    entries.push({
      name,
      method: get16(view, next + 10),
      compressedSize: get32(view, next + 20),
      localOffset: get32(view, next + 42)
    });
  }

  return entries;
}

async function readEntriesFromCentralDirectory(zip, centralEntries, onProgress) {
  const entries = [];
  const batchSize = 96;

  for (let start = 0; start < centralEntries.length; start += batchSize) {
    const batch = centralEntries.slice(start, start + batchSize).map((entry) => readCentralEntry(zip, entry));
    const results = await Promise.all(batch);

    for (const entry of results) {
      if (entry) entries.push(entry);
    }

    const done = Math.min(start + batchSize, centralEntries.length);
    const current = centralEntries[done - 1];
    onProgress(done, centralEntries.length, current ? `Extracting ${current.name}` : "");
  }

  return entries;
}

async function readCentralEntry(zip, central) {
  if (central.name.endsWith("/") || !isSupportedZipMethod(central.method)) return null;

  const view = viewOf(zip);
  const offset = central.localOffset;
  if (offset + 30 > zip.length || get32(view, offset) !== ZIP.LFH) return null;

  const nameLength = get16(view, offset + 26);
  const extraLength = get16(view, offset + 28);
  const dataStart = offset + 30 + nameLength + extraLength;
  const dataEnd = dataStart + central.compressedSize;

  if (central.compressedSize === 0xffffffff || dataEnd > zip.length) return null;

  try {
    const compressed = zip.subarray(dataStart, dataEnd);
    const data = central.method === 0 ? compressed : await inflateRaw(compressed);
    return { name: central.name, data: repairPayload(central.name, data) };
  } catch {
    return null;
  }
}

function repairPayload(name, data) {
  if (!name.toLowerCase().endsWith(".png")) return data;

  const repaired = repairPngCrcs(data);
  if (repaired !== data) {
    state.repairs.png += 1;
    setStatus(`Repairing ${name}`);
  }
  return repaired;
}

function repairPngCrcs(data) {
  if (!hasPngSignature(data)) return data;

  const sourceView = viewOf(data);
  let repaired = null;
  let offset = 8;

  while (offset + 12 <= data.length) {
    const length = sourceView.getUint32(offset, false);
    const typeStart = offset + 4;
    const dataStart = offset + 8;
    const crcOffset = dataStart + length;
    const nextOffset = crcOffset + 4;

    if (nextOffset > data.length) return repaired || data;

    const expected = crc32(data.subarray(typeStart, crcOffset));
    const actual = sourceView.getUint32(crcOffset, false);

    if (expected !== actual) {
      if (!repaired) repaired = data.slice();
      viewOf(repaired).setUint32(crcOffset, expected, false);
    }

    const type = textDecoder.decode(data.subarray(typeStart, dataStart));
    offset = nextOffset;
    if (type === "IEND") break;
  }

  return repaired || data;
}

function payloadCandidates(zip, view, dataStart, compressedSize, usesDescriptor, nextRecord) {
  if (!usesDescriptor && compressedSize !== 0xffffffff) {
    const dataEnd = dataStart + compressedSize;
    return dataEnd <= zip.length ? [zip.subarray(dataStart, dataEnd)] : [];
  }

  const hardEnd = nextRecord > dataStart ? nextRecord : zip.length;
  const ends = new Set([hardEnd]);

  for (const descriptorLength of [16, 12, 24, 20]) {
    const candidate = hardEnd - descriptorLength;
    if (candidate >= dataStart) ends.add(candidate);
  }

  if (hardEnd >= dataStart + 16 && get32(view, hardEnd - 16) === ZIP.DD) {
    ends.add(hardEnd - 16);
  }

  return [...ends].sort((a, b) => a - b).map((end) => zip.subarray(dataStart, end));
}

async function inflateRaw(payload) {
  if (!("DecompressionStream" in window)) {
    throw new Error("This browser does not support the built-in DecompressionStream API required for deflated ZIP entries.");
  }

  const stream = new Blob([payload]).stream().pipeThrough(new DecompressionStream("deflate-raw"));
  return new Uint8Array(await new Response(stream).arrayBuffer());
}

function buildStoredZip(entries) {
  const localParts = [];
  const centralParts = [];
  let offset = 0;

  for (const entry of entries) {
    const local = createLocalHeader(entry);
    const central = createCentralHeader(entry, offset);

    localParts.push(local, entry.data);
    centralParts.push(central);
    offset += local.length + entry.data.length;
  }

  return concatBytes([...localParts, ...centralParts, createEndRecord(entries.length, offset, byteLength(centralParts))]);
}

function createLocalHeader(entry) {
  const name = textEncoder.encode(entry.name);
  const header = new Uint8Array(30 + name.length);
  const view = viewOf(header);

  set32(view, 0, ZIP.LFH);
  set16(view, 4, 20);
  set16(view, 6, 0x0800);
  set16(view, 8, 0);
  set32(view, 14, crc32(entry.data));
  set32(view, 18, entry.data.length);
  set32(view, 22, entry.data.length);
  set16(view, 26, name.length);
  header.set(name, 30);

  return header;
}

function createCentralHeader(entry, localOffset) {
  const name = textEncoder.encode(entry.name);
  const header = new Uint8Array(46 + name.length);
  const view = viewOf(header);

  set32(view, 0, ZIP.CEN);
  set16(view, 4, 0x0314);
  set16(view, 6, 20);
  set16(view, 8, 0x0800);
  set16(view, 10, 0);
  set32(view, 16, crc32(entry.data));
  set32(view, 20, entry.data.length);
  set32(view, 24, entry.data.length);
  set16(view, 28, name.length);
  set32(view, 42, localOffset);
  header.set(name, 46);

  return header;
}

function createEndRecord(entryCount, centralOffset, centralSize) {
  const record = new Uint8Array(22);
  const view = viewOf(record);

  set32(view, 0, ZIP.EOCD);
  set16(view, 8, entryCount);
  set16(view, 10, entryCount);
  set32(view, 12, centralSize);
  set32(view, 16, centralOffset);

  return record;
}

function concatBytes(parts) {
  const output = new Uint8Array(byteLength(parts));
  let offset = 0;

  for (const part of parts) {
    output.set(part, offset);
    offset += part.length;
  }

  return output;
}

function byteLength(parts) {
  return parts.reduce((sum, part) => sum + part.length, 0);
}

function assertZipMagic(bytes) {
  if (bytes.length < 4 || bytes[0] !== 0x50 || bytes[1] !== 0x4b || bytes[2] !== 0x03 || bytes[3] !== 0x04) {
    throw new Error("The file does not start with ZIP local-file magic bytes: 50 4B 03 04.");
  }
}

function isSupportedZipMethod(method) {
  return method === 0 || method === 8;
}

function hasPngSignature(bytes) {
  return bytes.length >= 20 &&
    bytes[0] === 0x89 &&
    bytes[1] === 0x50 &&
    bytes[2] === 0x4e &&
    bytes[3] === 0x47 &&
    bytes[4] === 0x0d &&
    bytes[5] === 0x0a &&
    bytes[6] === 0x1a &&
    bytes[7] === 0x0a;
}

function findNextRecord(view, start) {
  let found = -1;
  for (const signature of [ZIP.LFH, ZIP.CEN, ZIP.EOCD]) {
    const offset = findSignature(view, start, signature);
    if (offset >= 0 && (found < 0 || offset < found)) found = offset;
  }
  return found;
}

function findSignature(view, start, signature) {
  for (let offset = start; offset + 4 <= view.byteLength; offset += 1) {
    if (get32(view, offset) === signature) return offset;
  }
  return -1;
}

function decodeZipName(bytes, flags) {
  return (flags & 0x0800 ? utf8Decoder : fallbackNameDecoder).decode(bytes);
}

function normalizeZipName(name) {
  const parts = [];

  for (const part of name.replace(/\\/g, "/").replace(/^\/+/, "").split("/")) {
    if (!part || part === ".") continue;
    if (part === "..") return "";
    parts.push(part);
  }

  return parts.join("/");
}

function crc32(bytes) {
  let crc = 0xffffffff;
  for (const byte of bytes) {
    crc = crcTable[(crc ^ byte) & 0xff] ^ (crc >>> 8);
  }
  return (crc ^ 0xffffffff) >>> 0;
}

function buildCrcTable() {
  const table = new Uint32Array(256);

  for (let i = 0; i < table.length; i += 1) {
    let c = i;
    for (let bit = 0; bit < 8; bit += 1) {
      c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
    }
    table[i] = c >>> 0;
  }

  return table;
}

function previewType(name) {
  const lower = name.toLowerCase();
  if (/\.(png|jpe?g|gif|webp|bmp)$/i.test(lower)) return "image";
  if (lower.endsWith(".ogg")) return "audio";
  if (lower.endsWith(".json") || lower.endsWith(".mcmeta")) return "json";
  if (/\.(txt|fsh|vsh|glsl|properties|lang|mcfunction)$/i.test(lower)) return "text";
  return "binary";
}

function fileExtension(name) {
  const leaf = name.split("/").pop() || name;
  const parts = leaf.split(".");
  if (parts.length < 2) return "FILE";

  const ext = parts.pop().toUpperCase();
  return ext === "MCMETA" ? "JSON" : ext;
}

function imageMime(name) {
  const lower = name.toLowerCase();
  if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
  if (lower.endsWith(".gif")) return "image/gif";
  if (lower.endsWith(".webp")) return "image/webp";
  if (lower.endsWith(".bmp")) return "image/bmp";
  return "image/png";
}

function downloadMime(name) {
  const type = previewType(name);
  if (type === "image") return imageMime(name);
  if (type === "audio") return "audio/ogg";
  if (type === "json") return "application/json";
  if (type === "text") return "text/plain";
  return "application/octet-stream";
}

function prettifyJson(data) {
  const text = textDecoder.decode(data);
  try {
    return JSON.stringify(JSON.parse(text), null, 2);
  } catch {
    return text;
  }
}

function recoveredZipName(name) {
  return name.replace(/\.zip$/i, "") + "-recovered.zip";
}

function downloadBlob(blob, name) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");

  link.href = url;
  link.download = name;
  document.body.append(link);
  link.click();
  link.remove();

  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

function setBusy(isBusy) {
  els.dropZone.classList.toggle("is-busy", isBusy);
  els.fileInput.disabled = isBusy;
}

function setStatus(text) {
  els.stateText.textContent = text;
}

function setProgress(percent) {
  if (percent <= 0) {
    state.progress = 0;
    els.meterFill.style.width = "0%";
    return;
  }

  state.progress = Math.max(state.progress, clamp(percent, 0, 100));
  els.meterFill.style.width = `${state.progress}%`;
}

function clamp(value, min, max) {
  return Math.max(min, Math.min(max, value));
}

function formatBytes(bytes) {
  if (bytes < 1024) return `${bytes} B`;

  const units = ["KB", "MB", "GB"];
  let value = bytes / 1024;
  let unit = units.shift();

  while (value >= 1024 && units.length) {
    value /= 1024;
    unit = units.shift();
  }

  return `${value.toFixed(value >= 10 ? 1 : 2)} ${unit}`;
}

function viewOf(bytes) {
  return new DataView(bytes.buffer, bytes.byteOffset, bytes.byteLength);
}

function get16(view, offset) {
  return view.getUint16(offset, true);
}

function get32(view, offset) {
  return view.getUint32(offset, true);
}

function set16(view, offset, value) {
  view.setUint16(offset, value, true);
}

function set32(view, offset, value) {
  view.setUint32(offset, value >>> 0, true);
}
