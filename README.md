# aero-extras

Adds #json and #aws-secret tags for aero.

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
 :json-secret #json #aws-secret "aws-json-secret-key"}
```

### JSON Library

Provides jsonista by default, but you can exclude this dependency and supply
cheshire instead.

```clojure
tggreene/aero-extras {:mvn/version "0.1.0"
                      :exclusions [metosin/jsonista]}
cheshire/cheshire {:mvn/version "5.10.1"}
```
