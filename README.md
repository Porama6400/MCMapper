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

| Argument             | Required | Note                                                |
| -------------------- | -------- | --------------------------------------------------- |
| -in server.jar       | yes      | Input jar file                                      |
| -map server.txt      | yes      | Obfuscation map file                                |
| -version <ver>       | no       | Version of the jar file                             |
| -client true         | no       | tell downloader to get client (default is server)   |
| -out output_path.jar | no       | Set output jar file                                 |
| -thread n            | no       | Set number of threads to run                        |
| -renamevar false     | no       | Rename local variable name based on class name      |
| -verify              | no       | Verify output jar file with ASM's CheckClassAdapter |
