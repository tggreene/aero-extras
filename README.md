# aero-extras

Adds `#concat`, `#format`, `#env-`, `#json` and `#aws-secret` tags for aero.

```
tggreene/aero-extras {:mvn/version "0.3.0"}
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

### AWS Library

`0.3.0` uses `amazonica` over `cognitect-labs/aws-api` as it doesn't appear to
support IMDSv2 ([issue](https://github.com/cognitect-labs/aws-api/issues/165)).

### JSON Library

Provides jsonista by default, but you can exclude this dependency and supply
cheshire instead.

```clojure
tggreene/aero-extras {:mvn/version "0.3.0"
                      :exclusions [metosin/jsonista]}
cheshire/cheshire {:mvn/version "5.10.1"}
```
