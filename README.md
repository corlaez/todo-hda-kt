# todo-hda-kt

This HDA ([Hypermedia Driven Application](https://htmx.org/essays/hypermedia-driven-applications/) is inspired by combining todomvc (frontend and backend) and https://htmx.org.

While this is a small and simple app and I could have easily done away with some libraries or simplified the code organization and architecture of the project, I built it this way to experiment with the libraries and how to work with them in a modular way

## Features:
- Very, very little javascript
- No JSON endpoints, just HTML (with htmx attributes)
- JVM Browser parallel testing (leveraging http4k and htmlunit)

## Architecture
While this is a small and simple app and I could have easily done away with some libraries or simplified the code organization and architecture of the project, I built it this way to experiment with the libraries and how to work with them in a modular way

I am using my own flavor of Hexagonal Architecture which separates the HTML endpoint code from the business logic and the database code.
This makes trivial to change http servers or database code, even on the fly. i.e. I inject a fake repository implementation to allow for parallel testing.
Another example is when I runned two servers side by side written in different libraries, see Previous Iterations at the end of this README.

## Stack (Kotlin):
- HTML String Generation:
  - kotlinx-html: DSL that allows me to generate HTML strings in a strongly typed way
  - Webjars: Allows me to pull npm libraries (todomvc css and htmx)
  - htmx.js: Enhance HTML, reduce JavaScript I would have to write otherwise 
- http4k: HTTP client/server library with a focus on "Server as a function" and dev experience
- koin: DI library
- Exposed: Database library with a focus on typesafe, portable queries
- sqlite-jdbc: embedded sqlite database and JDBC driver
- Testing:
  - htmlunit: A headless browser that runs in the jvm with support for CSS and JS
  
## Previous iterations
While writting this project I tried different libraries, each of them may have their own use cases but I refrained to making branches for these because they weren't my preferred (or complete) implementation and I don't want to draw the focus too much away from the master branch.
  
Still here are some commits that reflect some of these previous iterations in chronological order:
- [1](https://github.com/corlaez/htmx-todo-kt/commit/90e926eb2e7288a7dc5e4edbeba54fead012fe7f) Ktor http library, Dagger DI, Exposed db
- [2](https://github.com/corlaez/htmx-todo-kt/commit/652d432afcbc5fdc2425adda34815f22ec178dd8) Ktor http library, koin DI, Exposed db
- [3](https://github.com/corlaez/htmx-todo-kt/commit/f3202fcfc090dd567bf10d707408b270abcb0d68) Both Ktor and http4k http library (runs two servers), koin DI, Exposed db
  
