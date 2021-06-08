# Search-Engine-Project

The aim of this project is to develop a simple Crawler- based search engine that demonstrates the main features of a search engine (web crawling, indexing and ranking) and the interaction between them.

# How to Run the project:

## 1. Run the Query Processor Server:

- Must have java and tomcat installed..
- Go to 'Search_Engine/src' folder
- Compile the "QueryProcessor" java class
- Get .class file and put it in classes folder of WEB-INF of tomcat server;
- Also add the web.xml file there too (from src folder)
- And then run the tomcat server by running at cmd in bin folder ```startup.bat```

## 2. Run cors protection:

- Must have node installed..
- Go to 'cors' folder
- do ``` npm install cors-anywhere ```
- run ``` node cors ``` - now it's running on localhost:8000

## 3. Run Interface (Flutter Project):

- Must have flutter and dart installed..
- Go to 'Interface' folder
- run ``` flutter run --no-sound-null-safety -d chrome --web-port=5555 ``` - now it's running on localhost:5555
