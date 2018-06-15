# Capco Algo Trading Workshop #

Welcome to the Capco Algorithmic Trading Night of Chances 2018 WorkShop. Before we start, 
please follow the steps below in order to prepare for the workshop.

### Git installation ###
Before we start, we need to install git client on a machine in order to checkout the
source code of the application. Please, download the client from the following link:
[Git download page](https://git-scm.com/downloads)

After installation, you can checkout the repository running the following command from command
line in a directory you wish to download the sources to:

`git clone https://github.com/eriksuta/noc-algo-trader`

### Maven install ###
Our application uses maven as a build tool. In order to build the application from downloaded sources. To
install maven on your machine, download it from following link:
[Maven download page](https://maven.apache.org/download.cgi)

After download, install maven according to information in following link: 
[Maven install](https://maven.apache.org/install.html)
Make sure you can run the `mvn -v` in command line and see the version of installed maven.

### Building the application ###
Demo trading application is build using [Spring Boot](https://projects.spring.io/spring-boot/). There are
multiple ways to run the application, the first one is using an IDE (We prefer to work with IntelliJ IDEA,
but feel free to use your favourite IDE):
* Import and run application from IntelliJ IDEA: [IntelliJ Import](https://www.jetbrains.com/help/idea/maven.html#maven_import_project_start)

Other option is to build and run it from command line. Navigate to application root directory and run the following command:
* Build and run: `mvn spring-boot:run`

After this step, you should be able to navigate to `http://localhost:8080/` in your favourite browser and
see the following index page:
![Login Page](https://github.com/eriksuta/noc-algo-trader/blob/master/media/index-page.PNG "Login Page")

We will start our workshop from this step. Looking forward to seeing you all.


