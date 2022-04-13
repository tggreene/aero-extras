# aero-extras

Adds `#concat`, `#format`, `#env-`, `#json` and `#aws-secret` tags for aero.

```
tggreene/aero-extras {:mvn/version "0.2.0"}
```

## Usage

Include the namespace in your project:

```clojure
(ns project.main
  (:require tggreene/aero-extras))
```

Aero config:

```clojure
{:json-config #json "{\"a\": {\"b\": \"c\"}}"
 :secret #aws-secret "aws-secret-key"
 :json-secret #json #aws-secret "aws-json-secret-key"
 :concat #concat [[:a :b :c] [:d :e :f]]
 :format #format ["%s/%s" "/var/app" #env "SOME_PATH"]
 :env- #env- [HOME "/default/directory"]}
```

### JSON Library

Provides jsonista by default, but you can exclude this dependency and supply
cheshire instead.

```clojure
tggreene/aero-extras {:mvn/version "0.2.0"
                      :exclusions [metosin/jsonista]}
cheshire/cheshire {:mvn/version "5.10.1"}
```
