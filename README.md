<div align="center">
    <img src="https://i.imgur.com/jXG2L2p.png" height="200" width="200" alt="logo">
    <h1>MineCraft Resource Pack eXtractor (MCRPX)</h1>
    <strong>MCRPX is a tool to extract files from Minecraft: Java Edition resource packs into selected directory. It can also bypass corrupted resource packs, used by servers to protect their resource packs.</strong><br><br>
    <img src="https://img.shields.io/github/actions/workflow/status/Speedy11CZ/mcrpx/ci.yml?style=for-the-badge" alt="build status">
</div>

## Download
MCRPX can be downloaded [here](https://github.com/Speedy11CZ/mcrpx/releases/latest).
## How it works
It uses same method as Minecraft to load resource packs.
Because Minecraft must be able to load corrupted resource packs, this allows us to extract them too.

## How to use
This tool can be used only in **GUI** *(recommended)* or in **CLI**. You need to have Java 17 or higher installed on your computer.

## Building
For building this project, you need JDK 8 or newer.
Then, just run script `gradlew build` in project directory.
You can find compiled JAR files in `*/build/libs` directories.

## Warning
Despite the fact that this tool allows extracting corrupted resource packs, please note that the content may be subject to copyright. Use this tool at your own risk.