(ns narodnik-core.exchange
  (:use 
   [lamina core api]
   [narodnik-core library]
   [narodnik-core data]))

(use '[clojure.java.shell :only [sh]])

(comment "Narodnik packages"...
"Functions here are to be api for building
narodnik-reliant runtime client packages, the 
user-agent/bot DSL." )

(def slave-task-cache (atom #{}))

(comment 
  (def sample-task 
    {:taskid 10667 :command "Nycommand"})

  (def other-task
    {:taskid 10668 :command "anderecommand"})
  )


(defn make-task [content] 
  {:content content :id (db-generate-id :task)})

(defn make-job [task machine]{
                              :slavetype "machine"
                              :slaveid (str (:name machine) )
                              :taskid (:id task)
                              :status "undone"
                              :starttime (datetime-now)
                              :endtime "NULL"
                              })

(defn give-all-job [message]
  (println "Giving all machines job : " message)
  (let [machines (db-select-all :machine)
        task (make-task message)]
    (map (fn [slave] (let [task (make-task message)]
                  (db-insert! :task task)
                  (db-insert! :job (make-job task slave))))
         machines))) 

(defn greet-slave-job! [machine]
           (let [task (make-task "\"Greetings.\"")] 
             (let [greeting (make-job task machine) ]
               (db-insert! :job greeting)
               (db-insert! :task task))))

;; ---------------------- concerns of machine ^ and slave \/ are muddled 

; System I/O calls on Winblows only for now
;; remember to develop on 
(defn system-call [command] 
  (:out (sh "cmd" "/C" command)))

(comment "windows location"
         "C:/Program Files (x86)/Mozilla Firefox/firefox.exe")

(defn find-firefox-location []
  "firefox")

(defn get-user-name []
  (clojure.string/trim-newline 
   (system-call "echo %username%")))

(defn find-userscript-location []
  (str "C:/Users/" (get-user-name)
       "/AppData/Roaming/Mozilla/Firefox/Profiles/x1d8zrt8.default/scriptish_scripts/testjs/"))

(defn userscript-template [name target message]
(str
"// ==UserScript==
// @name " name "
// @namespace   narodnik/scripts
// @description a Narodnik firefox
// @include     " target "
// @version     1
// @grant       none
// ==/UserScript==

alert(\"" message "\")
"))

(defn run-firefox-page [page]
  (let [firefox-str (find-firefox-location)]  
    (sh firefox-str page)))

(defn get-form-names [pagename])

(defn test-method []
  (println "HELLO NARODNIK!")
  (comment run-firefox-page "http://www.google.com"))

(comment
  (run-firefox-page "http://www.google.com"))

(defn test-package []
    (give-all-job "test-package"))

(test-package)

(defn return-template-list [http-channel]
  (comment
  (println "Returning forms...")
  (attempt "returning forms"
              (enqueue http-channel
                       {:status 200
                        :headers {"content-type" "text/html"}
                        :body (str 
                               (map :template @slave-task-cache))}))))

(defn return-form-list [http-channel template-name]
  (comment
  (let [template-set (filter (fn [x] (= (:template x) template-name)) @slave-cache)]
    (let [form-list 
          (interleave
           (map :name (:forms template-set))
           (map :content (:forms template-set)))]
      (enqueue http-channel {
                             :status 200 
                             :headers {"content-type" "text/plain"}
                             :body (str form-list)})))))

(defn slave-api [command]
  (cond 
   (= command "test-package") (test-method)))
