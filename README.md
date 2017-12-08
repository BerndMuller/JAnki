# JAnki

A simple library to use Anki Overdrive with Java

## Getting Started

The class ``de.pdbm.janki.core.Vehicle`` is JAnki's public API and should be your starting point.

### Prerequisites

JAnki uses TinyB <https://github.com/intel-iot-devkit/tinyb> which you have to install.
Please ensure that you have read the troubleshooting guide of TinyB.

You have to install tinyb.jar into your local Maven repository. You can do this manually 
or copy tinyb.jar to the lib folder of this project and run

```
mvn install:install-file@install-external
```

TinyB uses BlueZ, a Linux Bluetooth stack, which you also have to install. Usually BlueZ is included in your Linux distribution.

### Installing

Since we use Maven simple do

```
mvn clean package
```

or

```
mvn clean install
```

## License

ich überlege noch

