# MCMapper
Minecraft mapping tool to deobfuscate jar file with Mojang provided map file

***

**I am aware that currently it is not working with 1.18+, this will be fix soon**

## Compiling
> gradle build

**Binary output can be found here:**
* MCMapper CUI: ./core/build/libs/MCMapper-XYZ-all.jar
* MCMapper GUI: ./gui/build/libs/MCMapperGUI-XYZ-all.jar

## Usage

Simple use with auto-downloader:
> java -jar MCMapper.jar -version 1.17

To map a client jar file, use:
> java -jar MCMapper.jar -version 1.17 -client

Note: `-in` and `-map` option can also be use to specify location to download

To run mapper on existing jar and map file:
> java -jar MCMapper.jar -in server.jar -map server-map.txt


This will map _server.jar_ into _server-out.jar_ with according to _server-map.txt_

| Argument             | Required | Note                                                |
| -------------------- | -------- | --------------------------------------------------- |
| -in server.jar       | yes      | Input jar file                                      |
| -map server.txt      | yes      | Obfuscation map file                                |
| -version <ver>       | no       | Version of the jar file                             |
| -client              | no       | tell downloader to get client (default is server)   |
| -out output_path.jar | no       | Set output jar file                                 |
| -thread n            | no       | Set number of threads to run                        |
| -renamevar           | no       | Rename local variable name based on class name      |
| -verify              | no       | Verify output jar file with ASM's CheckClassAdapter |
