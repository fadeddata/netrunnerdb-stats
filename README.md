# netrunnerdb-stats

You'll need to install sbt. I like using homebrew

```
$ brew install sbt
```

Then run sbt while in the netrunnerdb-stats cloned folder
```
$ cd path/to/repo
$ sbt
```
at the sbt console you can use "run" to start the app.

```
[play-scala] $ run
```

It'll listen on port 9000. So the following two URL's should return some data:

http://localhost:9000/popular-sets

http://localhost:9000/popular-cards

Either of these will take some time to scrape but there is a caching layer (simple object persistence to disk library) that will make subsequent requests fast (some files are created in the root of the project that have been added to the .gitignore). 