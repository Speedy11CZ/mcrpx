<div align="center">
    <img src="https://i.imgur.com/jXG2L2p.png" height="200" width="200" alt="logo">
    <h1>MineCraft Resource Pack eXtractor (MCRPX)</h1>
    <strong>MCRPX is a tool to extract files from Minecraft resource packs into selected directory. It can also bypass corrupted resource packs, used by servers to protect their resource packs.</strong><br><br>
    <img src="https://forthebadge.com/images/badges/made-with-java.svg" height="30" alt="made with Java">&nbsp;
    <img src="https://img.shields.io/github/workflow/status/Speedy11CZ/mcrpx/Build%20Gradle%20and%20Publish?style=for-the-badge" alt="build status">
</div>

## How it works
It uses same method as Minecraft to load resource packs.
Because Minecraft must be able to load corrupted resource packs, this allows us to extract them too.

## How to use
This tool can be used only in command line. You need to have Java installed on your computer.
```bash
Option (* = required)                   Description                       
---------------------                   -----------                       
-?, --help                              Show the help
-mc, -minecraft                         Extract resource pack from Minecraft jar
* -i, --input <file>                    Input file                                              
-o, --output <file> (default: output)   Output directory for resource pack    
```

### Examples
`java -jar MCRPX.jar -i my-resource-pack.zip -o my-resource-pack`

`java -jar MCRPX.jar -i 1.18.8.jar -o 1.18.8-resource-pack -mc`

## Building
For building this project, you need JDK 8 or newer.
Then, just run script `gradlew build` in project directory.
You can find compiled JAR file in `build/libs` directory.

## Warning
Despite the fact that this tool allows extracting corrupted resource packs, please note that the content may be subject to copyright. Use this tool at your own risk.