(ns kp-scout.core
  (:gen-class)
  (:use [clojure.tools.cli :only (cli)]
        clojure.set
        hickory.core
        clojure.pprint
        postal.core)
  (:require [clj-http.client :as client]
            [hickory.select :as s]))

(def loginUrl "https://mydoctor.kaiserpermanente.org/cyd/displayLogin.action")
(def searchFormUrl "https://mydoctor.kaiserpermanente.org/cyd/nonMemberSearch.action")
(def searchUrl "https://mydoctor.kaiserpermanente.org/cyd/ajaxNonMemberSearchResults.action")
(def loadSearchResultsUrl "https://mydoctor.kaiserpermanente.org/cyd/ajaxLoadSearchResults.action")

(defn- extract-security-key [body]
  (:value (:attrs (first (s/select
                           (s/and
                             (s/tag :input)
                             (s/id :securityKey))
                           (as-hickory (parse body)))))))

(defn- send-mail [opts]
  (println (send-message {:host (:host opts)
                 :port (:port opts)
                 :ssl :yes!!!11
                 :user (:user opts)
                 :pass (:password opts)}
                {:from (:from opts)
                 :to (:to opts)
                 :subject "Kaiser doctor found!"
                 :body (str "Found Kaiser doctor " (:name opts))})))

(defn- get-doctors [zip]
  (let [cs (clj-http.cookies/cookie-store)
        _ (client/get loginUrl {:cookie-store cs})
        searchForm (client/get searchFormUrl {:cookie-store cs})
        securityKey (extract-security-key (:body searchForm))]
    (loop [pageNumber 1
           doctors []]
      (let [searchResult (if (= pageNumber 1)
                           (client/post searchUrl {:cookie-store cs
                                                   :form-params {:speciality "PED"
                                                                 :zipcode zip
                                                                 :distance 1
                                                                 :securityKey securityKey
                                                                 :kaiserKey "Hidden Value"}})
                           (client/post loadSearchResultsUrl {:cookie-store cs
                                                              :query-params {:pageAction ""
                                                                             :pageNumber pageNumber
                                                                             :securityKey securityKey}}))
            doctorsOnPage (map #(first (:content %)) (s/select
                                                       (s/and
                                                         (s/tag :h3)
                                                         (s/class "title"))
                                                       (as-hickory (parse (:body searchResult)))))
            doctors (concat doctors doctorsOnPage)]
        (if (empty? doctorsOnPage)
          doctors
          (recur (inc pageNumber) doctors))))))

(defn- check [opts]
  "Checks to see if a doctor exists for the name and zip code"
  (let [doctors (get-doctors (:zip opts))
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
             ["-n" "--name" "Doctor Name"]
             ["-z" "--zip" "Zip code" :parse-fn #(Integer/parseInt %)])]
    (if (= (count opts) 8)
      (check opts)
      (println banner))))