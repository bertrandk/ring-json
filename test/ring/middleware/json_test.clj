(ns ring.middleware.json-test
  (:use clojure.test
        ring.middleware.json)
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]))

(def sample-json "{\"name\":\"John Doe\",\"age\":40,\"gender\":\"M\",\"items\":
                 [\"hat\",\"shirt\",\"pants\",\"shoes\"]}")

(defn req
  [& {:keys [server-port server-name remote-addr uri scheme request-method
             content-type headers body] :or {server-port 80 server-name
                                             "localhost" remote-addr "127.0.0.1"
                                             uri "/index" scheme :http
                                             request-method :get content-type ""
                                             headers {} body ""}}]
  {:server-port server-port
   :server-name server-name
   :remote-addr remote-addr
   :uri uri
   :scheme scheme
   :request-method request-method
   :content-type content-type
   :headers headers
   :body body})

(deftest test-wrap-json

         (testing "json-request-headers"
                  (let [handler (fn [req] {:status 200 :headers {}
                                           :body (dissoc req :body)})
                        req (req :content-type "application/json"
                                 :body (io/input-stream
                                         (.getBytes sample-json)))
                        resp ((wrap-json handler) req)
                        req-headers (json/parse-string (resp :body))]
                    (is (= (req-headers "json-params")
                           (json/parse-string sample-json)))
                    (is (= (req-headers "params")
                           (json/parse-string sample-json)))))

         (testing "json-request-headers-with-other-params"
                  (let [handler (fn [req] {:status 200 :headers {}
                                           :body (dissoc req :body)})
                        req (assoc (req :content-type "application/json"
                                        :body (io/input-stream
                                                (.getBytes sample-json)))
                                   :params {"Detective" "Columbo"})
                        resp ((wrap-json handler) req)
                        req-headers (json/parse-string (resp :body))]
                    (is (= (req-headers "json-params")
                           (json/parse-string sample-json)))
                    (is (= (req-headers "params")
                           (assoc (json/parse-string sample-json)
                                  "Detective" "Columbo")))))

         (testing "custom-json-request-headers"
                  (let [handler (fn [req] {:status 200 :headers {}
                                           :body (dissoc req :body)})
                        req (req :content-type "application/vnd.custom+json"
                                 :body (io/input-stream
                                         (.getBytes sample-json)))
                        resp ((wrap-json handler) req)
                        req-headers (json/parse-string (resp :body))]
                    (is (= (req-headers "json-params")
                           (json/parse-string sample-json)))
                    (is (= (req-headers "params")
                           (json/parse-string sample-json)))))

         (testing "json-request-with-empty-body"
                  (let [handler (fn [req] {:status 200 :headers {}
                                           :body req})
                        req (dissoc (req :content-type "application/json")
                                    :body)
                        resp ((wrap-json handler) req)
                        req-headers (json/parse-string (resp :body))]
                    (is (nil? (req-headers "json-params")))
                    (is (nil? (req-headers "params")))))

         (testing "string-response"
                  (let [sample-string "This is a string"
                        handler (constantly {:status 200 :headers {}
                                             :body sample-string})
                        req (req)
                        resp ((wrap-json handler) req)]
                    (is (= (resp :body) (json/generate-string sample-string)))))

         (testing "vector-response"
                  (let [sample-vector ["This" "is" 1 "vector"]
                        handler (constantly {:status 200 :headers {}
                                             :body sample-vector})
                        req (req)
                        resp ((wrap-json handler) req)]
                    (is (= (resp :body) (json/generate-string sample-vector)))))

         (testing "map-response"
                  (let [sample-map {:fname "John" "lname" "Doe" :age 40}
                        handler (constantly {:status 200 :headers {}
                                             :body sample-map})
                        req (req)
                        resp ((wrap-json handler) req)]
                    (is (= (resp :body) (json/generate-string sample-map))))))
