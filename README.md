<div align="center">
    <img src="https://i.imgur.com/jXG2L2p.png" height="200" width="200" alt="logo">
    <h1>MineCraft Resource Pack eXtractor (MCRPX)</h1>
    <strong>MCRPX is a tool to extract files from Minecraft resource packs into selected directory. It can bypass corrupted resource packs, used by servers to protect their resource packs.</strong><br><br>
    <img src="https://forthebadge.com/images/badges/made-with-java.svg" height="30" alt="MADE WITH JAVA">&nbsp;
</div>

## How it works
It uses same method as Minecraft to load resource packs.
Because Minecraft must be able to load corrupted resource packs, this allows us to extract them too.

## How to use
```bash
Option (* = required)                   Description                       
---------------------                   -----------                       
-?, --help                              Show the help                     
* -i, --input <file>                    Input resource pack                                               
-o, --output <file> (default: output)   Output directory for resource pack    
```

## Building
For building this project, you need JDK 8 or newer.
Then, just run script `gradlew build` in project directory.
You can find compiled JAR file in `build/libs` directory.

## Warning
Despite the fact that this tool allows extracting corrupted resource packs, please note that the content may be subject to copyright. Use this tool at your own risk.