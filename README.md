# MCMapper
Minecraft mapping tool to deobfuscate jar file with Mojang provided map file

***

## Compiling
> gradle shadowJar


## Usage

To show help, use
> java -jar MCMapper.jar -help

To run mapper, use
> java -jar MCMapper.jar -in server.jar -map server-map.txt

This will map _server.jar_ into _server-out.jar_ with according to _server-map.txt_

| Argument             | Required | Note                                                |
| -------------------- | -------- | --------------------------------------------------- |
| -in server.jar       | yes      | Input jar file                                      |
| -map server.txt      | yes      | Obfuscation map file                                |
| -out output_path.jar | no       | Set output jar file                                 |
| -thread n            | no       | Set number of threads to run                        |
| -verify              | no       | Verify output jar file with ASM's CheckClassAdapter |
