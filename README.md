# chat_cliente-servidor

## Descrição
Um sistema de chat usando a arquitetura cliente-servidor por meio de sockets.

## Requisitos
- Java JDK 17.0.15
- Duas máquinas virtuais na mesma rede
- IPs na mesma sub-rede (ex.: 192.168.1.0/24)

## Compilação
```bash
javac -d out Server.java
javac -d out Client.java
```

## Execução
````bash
java -cp out Server
java -cp out Client
````
