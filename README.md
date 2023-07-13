# MCMapper
Minecraft mapping tool to deobfuscate jar file with Mojang provided map file

***

## Compiling
> gradle build

**Binary output can be found here:**
* MCMapper CLI: ./core/build/libs/MCMapper-XYZ-all.jar

## Usage

Simple use with auto-downloader:
> java -jar MCMapper.jar -version 1.20.1

To map a client jar file, use:
> java -jar MCMapper.jar -version 1.20.1 -client

Note: `-in` and `-map` option can also be use to specify location to download

To run mapper on existing jar and map file:
> java -jar MCMapper.jar -in server.jar -map server-map.txt


This will map _server.jar_ into _server-out.jar_ with according to _server-map.txt_

| Argument             | Note                                                |
| -------------------- | --------------------------------------------------- |
| -in server.jar       | Input jar file                                      |
| -map server.txt      | Obfuscation map file                                |
| -version <ver>       | Version of the jar file                             |
| -client true         | tell downloader to get client (default is server)   |
| -out output_path.jar | Set output jar file                                 |
| -thread n            | Set number of threads to run                        |
| -renamevar false     | Rename local variable name based on class name      |
| -verify              | Verify output jar file with ASM's CheckClassAdapter |
