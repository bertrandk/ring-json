# Ring-Json

Middleware that augments Ring requests and responses with JSON support.

## Installation

To use this libary, add the following to your Leingingen `:dependencies`:

    [bk/ring-json "0.1.0"]

## Usage

The  `wrap-json` middleware will typically be applied to your
application handler as below.

```clojure
(ns app.core
  (:use  [ring.middleware.json]
         [ring.middleware params
                          keyword-params
                          nested-params]))

(def app
  (-> handler
    (wrap-keyword-params)
    (wrap-nested-params)
    (wrap-params)
    (wrap-json)))
```

The middleware will parse the body of JSON requests and
makes the JSON key-values available to your routes through the `json-params` and
`params` keys on the request headers.

```clojure
;; For the request:
(def original-req
  {:server-port 80
   :server-name "localhost"
   :remote-addr "127.0.0.1"
   :uri "/users"
   :scheme :http
   :request-method :post
   :content-type "application/json"
   :headers {"content-type" "application/json"}
   ; :body will be exposed to your routes as an InputStream
   :body "{\"name\":\"John Doe\",\"age\":40,\"gender\":\"M\"]}"})

;; Your routes will see:
(def modified-req
  {:server-port 80
   :server-name "localhost"
   :remote-addr "127.0.0.1"
   :uri "/users"
   :scheme :http
   :request-method :post
   :content-type "application/json"
   ; ***
   :headers {"content-type" "application/json" 
             "json-params" {"name" "John Doe" "age" 40 "gender" "M"}
             "params" {"name" "John Doe" "age" 40 "gender" "M"}}
   ; ***
   ; :body will be exposed to your routes as an InputStream
   :body "{\"name\":\"John Doe\",\"age\":40,\"gender\":\"M\"]}"})
```

The middleware will also convert String and ISeq responses to the
appropriate JSON representation.

```clojure
;; String response
(defn string-handler
  [req]
  {:status 200
   :headers {}
   :body "This is a string"}) ;; => {:status 200 
                              ;;     :headers {"content-type"
                              ;;               "application/json"}
                              ;;     :body "\"This is a string\""}

;; Vector response
(defn vector-handler
  [req]
  {:status 200
   :headers {}
   :body ["This is" 1 "vector"]) ;; => {:status 200 
                                 ;;     :headers {"content-type"
                                 ;;               "application/json"}
                                 ;;     :body "[\"This is\",1,\"vector\"]"}

;; Map response
(defn map-handler
  [req]
  {:status 200
   :headers {}
   :body {:name "John" :age 40}) ;; => {:status 200 
                                 ;;     :headers {"content-type"
                                 ;;               "application/json"}
                                 ;;     :body "{\"age\":40,\"name\":\"John\"}"}
```

## License

Copyright © 2013 Bertrand Karerangabo

Distributed under the MIT License (see LICENSE).
