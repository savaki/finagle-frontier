Frontier
========

Frontier (Front-Tier) is a routing proxy server written in finagle that allows you to compose other services behind it.

## Quick Start

Suppose we have two applications a corporate website and our e-commerce app.  We'd like them to both to be deployed on www.mycompany.com, but we don't want to couple their release cycles.  Here's where Frontier comes in.

Using a simple json file, we can instruct Frontier on how we'd like traffic routed between our services.

#### sample.json

```
{
  "territories": [
    {
      "port": 3000,
      "trail": [
        {
          "locations": ["/blog/*"],
          "hosts": ["10.0.1.5:4002"]
        },
        {
          "hosts": ["10.0.1.5:4002"]
        }
      ]
    }
  ]
}
```

And we can execute it as follows:

```
java -jar frontier-0.1.jar --config sample.json
```

In the first part of sample.json, we define a territory.  In Frontier, a Territory is a proxy service for a set of services listening to a specific port.  The key really is the port.  

1 Territory : 1 Port

Inside the territory configuration, we see two properties, port and trail.  The port is the local port to listen on.  The hosts are the hosts that are to be proxied.  

You can see that trails contains an array of two elements.  In the first element, we're instructing Frontier that our blog is hosted at 10.0.1.5:4002 and traffic to /blog/* should be routed to the blog.

In the second element, you'll notice we just have a host and port.  When we omit the locations, we're telling Frontier that any request regardless of URI should be proxied to the specified host.

Lastly, the Frontier will only proxy to the first match found.

## Building Frontier

Frontier requires that you have a JDK 1.6 or better installed.  To build the jar file, simply type:

```
./sbt assembly
```

The jar will be created to

```
frontier-app/target/scala-2.10/frontier-0.1-SNAPSHOT.jar
```

## Decorators

One of the challenges with having multiple services stitched together is maintaining a single look and feel across all your applications.  Often stylesheets and templates are copied or shared between applications.  Frontier provides decorators that assist in managing this.  Decorators (or layouts in Rails-speak) all look and feel to be centralized in one spot.

Let's take a look at our previous application and see how we might apply decorators to centralize the look and feel:


```
{
  "template-factories": ["ws.frontier.template.velocity.VelocityTemplateFactory"],
  
  "decorators": {
    "default": {
      "content_type": "text/html",
      "type": "velocity",
      "uri": "http://10.0.1.5:4002/shell.html"
  	}
  },
  
  "territories": [
    {
      "port": 3000,
      "trail": [
        {
          "decorators": ["default"],
          "locations": ["/blog/*"],
          "hosts": ["10.0.1.5:4002"]
        },
        {
          "hosts": ["10.0.1.5:4002"]
        }
      ]
    }
  ]
}
```

So what did we change?  You'll see at the top level we added two new elements.  The first is template-factories.  Template Factories enable us to choose which templating language we'd like to use.  In this example, we've chosen Apache Velocity as our templating engine.  It's a solid, fast, simple engine.

The next new element is called decorators.  Here's where we define the layouts that we'd like to use.  You'll see we've defined a singe layout, default, and given it a few settings.  content_type says only decorate text/html content so images, pdfs, etc would remain untouched.  type specifies which template language to use.   Here, we're specifying velocity.  And lastly, the uri specifies where we should pull the template from.  Note that any valid URL will be acceptable.

Here's what shell might look like:

#### shell.html

```
stick some html above
${content}
and some html below
```

Clearly this isn't a real shell, but you can see how the layout would be applied.  **content** is a magic property set by Frontier to define the content being proxied.  ${content} is velocity's way of rendering content.  In handlebars, we would write this as {{content}}

The last change from the previous json file is the addition of a decorators element inside the trail.  This is where we specify what should be decorated.  In this example, the blog is decorated, but the website is not.  There's certainly no reason why both couldn't be decorated.

## Frequently Asked Questions

* Can a page tell if it's being proxied by Frontier?  Yes.  Frontier passes an X-Frontier request header with the value being the Frontier version.

```
X-Frontier: 0.1
```

## Roadmap

Here's some of the things that we're considering for Frontier in the future:

* session management
* zero downtime deployments
