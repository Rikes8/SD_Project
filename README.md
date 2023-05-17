### Motor de pesquisa de páginas Web

Instalação e execução do projeto.

***

Execução:
---

Para executar o projeto(meta1) deve abrir a seguinte pasta: 
* "\SD_Project\"

e executar os seguintes comandos na consola:

* Search Module: java -jar .\out\artifacts\SearchModule_jar\SearchModule.jar
* Queue: java -jar .\out\artifacts\Queue_jar\Queue.jar
* Downloader: java -jar .\out\artifacts\Downloader_jar\Downloader.jar
* Storage Barrel: java -jar .\out\artifacts\StorageBarrel_jar\StorageBarrel.jar
* Client: java -jar .\out\artifacts\Client_jar\Client.jar

Commandos:
--
* Pesquisar por palavras:
    * search word ... word 
* Registar user
    * register username password
* LogIn user
    * login username password
* Links apontados
    * conn url
* Indexar link
    * index url
* Estatísticas servidor
    * stats
* Estatísticas de pesquisa
    * statsv2

Dependência:
---
* jsoup-1.15.4.jar - https://jsoup.org/download


Antes de correr a aplicação da meta2 deve ter executado o comandos da meta 1 pela seguinte ordem:
SearchModule > StorageBarrel > Queue > Downloader

Para executar o projeto(meta2) deve abrir a seguinte pasta: 
"\Meta2_SD\src\main\java\com\example\demo\"

E correr DemoApplication.java no seu IDE

Dependencia:
SearchModule.jar - "SD\SD_Project\out\artifacts\SearchModule_jar"

