# lein-nrepl

A Leiningen plugin to start an [nREPL][] server.

## Why?

Some of you might be wondering why is this plugin needed - after all `lein repl` starts an
nREPL server, doesn't it?

The problem is that `lein repl` is still not updated to work with
nREPL 0.4+ (see https://github.com/technomancy/leiningen/pull/2444),
which means that in the mean time it's hard for people who want to run
the new nREPL to do so.

This plugin is very minimalistic, doesn't aim to replicate all of the
functionality of `lein repl`, but it gets the job done (and it hopefully won't be needed
forever).

## Usage

Put `[nrepl/lein-nrepl "0.2.0"]` into the `:plugins` vector of your `:user`
profile.

Afterwards run the following command:

    $ lein nrepl

That will start an nREPL server with on a random port and connect
a [REPLy](https://github.com/trptcolin/reply)-powered REPL to it.

### Supported Options

* `:port` — defaults to 0, which autoselects an open port

* `:bind` — bind address, by default `"::"` (falling back to "localhost" if
  "::" isn't resolved by the underlying network stack)

* `:handler` — the nREPL message handler to use for each incoming connection;
  defaults to the result of `(nrepl.server/default-handler)`

* `:middleware` — a sequence of vars or string which can be resolved to vars,
representing middleware you wish to mix in to the nREPL handler. Vars can
resolve to a sequence of vars, in which case they'll be flattened into the
list of middleware.

* `:headless` - Defaults to `false`. Controls whether to start an interactive
REPL (powered by REPLy) or not.

* `:block` — Defaults to `true`. Set it to `false` for relinquishing control
  to the next Leiningen task: e.g `lein do nrepl :block false, test-refresh`.
  Note that with a `false` value and no next Lein task to run,
  lein-nrepl will immediately close. This option is ignored unless `:headless`
  is also true.

### Using with CIDER

You can start a CIDER-capable server like this:

    $ lein nrepl :middleware "['cider.nrepl/cider-middleware]"

Note that this currently requires `cider-nrepl` 0.18.0+ to be in your deps,
as earlier `cider-nrepl` releases depend on the legacy `tools.nrepl`. You can simply put
the dependency in your `:dev` profile (it should be a regular dependency, not a plugin).

Afterwards you can simply do `C-c C-x c c` to connect from CIDER to the running server.

Using this with `cider-jack-in` is a bit more involved currently as
you can't just replace `lein repl` with `lein nrepl` in your CIDER
config, because they function a bit differently.  If you want to use
`lein nrepl` with `cider-jack-in` it's current best to simply disable
`cider-inject-dependencies-at-jack-in` and rely on deps specified in
your profiles. Here's a simple Emacs config:

```
(setq cider-inject-dependencies-at-jack-in nil)
(setq lein-parameters "nrepl")
```

And here's a sample `profiles.clj` file for you:

``` clojure
{:user
 {:dependencies [[cider/cider-nrepl "0.18.0"]]}
 {:plugins [[nrepl/lein-nrepl "0.2.0"]]}}
```

## License

Copyright © 2018 Bozhidar Batsov

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[nREPL]: https://github.com/nrepl/nREPL
