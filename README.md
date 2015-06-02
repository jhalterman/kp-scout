# kp-scout

Getting the [Kaiser Permanente](http://kp.org) doctor you want is a crazy process that involves repeatedly checking the [KP doctor website](https://mydoctor.kaiserpermanente.org/cyd). kp-scout does that for you, sending you an e-mail when the doctor you want is available.

## Setup

```
lein uberjar
```

## Usage

```
 Switches        Description
 --------        -----------         
 -h, --host      SMTP host           
 -p, --port      SMTP port           
 -u, --user      SMTP user           
 -s, --password  SMTP password       
 -f, --from      E-Mail from address 
 -t, --to        E-Mail to address   
 -n, --name      Doctor Name         
 -z, --zip       Zip code  
```

### Example

```
java -jar target/kp-scout.jar \
     -n Who -z 94101 \
	 -h smtp.comcast.net -p 465 -u joe -s abc123 -f joe@comcast.com -t joe@comcast.com
```

Scheduling needs to be done externally, via cron or the like.

## License

Copyright © 2015 Jonathan Halterman

Distributed under the Eclipse Public License 1.0
