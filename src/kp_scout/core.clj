(ns kp-scout.core
  (:gen-class)
  (:use [clojure.tools.cli :only (cli)]
        clojure.set
        clojure.pprint
        postal.core)
  (:require [clj-http.client :as client]
            [cheshire.core :as json]))

(def searchUrlFormat "https://mydoctor.kaiserpermanente.org/ncal/mdo/cyd/jsonService.jsp?serviceNm=getAvailableProviders.json&jsonInput=^~\"tokenId\":\"\",\"deptId\":\"%s\",\"facId\":\"\",\"zipCode\":\"%s\",\"distance\":\"15\",\"slotsInPage\":\"1000\",\"showFirstField\":\"default\",\"showFirstValue\":\"\"$$&clientShortNm=ncalcydnew")

(defn- send-mail [opts]
  (println (send-message {:host (:host opts)
                          :port (:port opts)
                          :ssl  :yes!!!11
                          :user (:user opts)
                          :pass (:password opts)}
                         {:from    (:from opts)
                          :to      (:to opts)
                          :subject "Kaiser doctor found!"
                          :body    (str "Found Kaiser doctor " (:name opts))})))

(defn- get-doctors [zip speciality]
  (let [searchString (format searchUrlFormat speciality zip)
        searchResult (client/get searchString)
        searchResultJson (json/parse-string (:body searchResult))
        doctorInfo (get (first (get searchResultJson "jsonResponse"))
                        "availableProviderSlots")]
    (map #(get % "providerName") doctorInfo)))

(defn- check [opts]
  "Checks to see if a doctor exists for the name and zip code"
  (let [doctors (get-doctors (:zip opts) (:speciality opts))
        docFound (some true? (map #(.contains (.toLowerCase %) (.toLowerCase (:name opts))) doctors))]
    (if docFound
      (do
        (println "Found doctor.")
        (send-mail opts))
      (println "Doctor not found."))))

(defn -main [& args]
  (let [[opts args banner]
        (cli args
             ["-h" "--host" "SMTP host"]
             ["-p" "--port" "SMTP port" :parse-fn #(Integer/parseInt %)]
             ["-u" "--user" "SMTP user"]
             ["-s" "--password" "SMTP password"]
             ["-f" "--from" "E-Mail from address"]
             ["-t" "--to" "E-Mail to address"]
             ["-a" "--speciality" "Speciality - MED, PED or GYN"]
             ["-n" "--name" "Doctor Name"]
             ["-z" "--zip" "Zip code" :parse-fn #(Integer/parseInt %)])]
    (if (= (count opts) 9)
      (check opts)
      (println banner))))