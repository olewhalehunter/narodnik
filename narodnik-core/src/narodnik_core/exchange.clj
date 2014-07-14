(ns narodnik-core.exchange
  (:use 
   [lamina core api]
   [narodnik-core library]
   [narodnik-core data]))

(comment "Narodnik packages"...
"Functions here are to be api for building
narodnik-reliant runtime client packages, the 
user-agent/bot DSL." )

(def slave-cache (atom []))
(def task-cache (atom {}))

(use '[clojure.java.shell :only [sh]])

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

(defn merge-slave-state [machine]
  (let [state (db-select-all :dictionary)]
    (let [task (make-task (str "(merge-slave-cache " state))]
      (let [state-update-job (make-job task machine)]
        (db-insert! :job state-update-job)
        (db-insert! :task task)))))

(defn greet-slave-job! [machine]
           (let [task (make-task "\"Greetings.\"")] 
             (let [greeting (make-job task machine) ]
               (db-insert! :job greeting)
               (db-insert! :task task))))

; System I/O calls on Winblows only for now

(defn system-call [command] 
  (:out (sh "cmd" "/C" command)))

(defn find-firefox-location []
  "C:/Program Files (x86)/Mozilla Firefox/firefox.exe")

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
  (println "HELLO NARODNIK!"))

(defn test-package []
    (give-all-job "test-package"))

(defn return-template-list [http-channel]
  (println "Returning forms...")
  (attempt "returning forms"
              (enqueue http-channel
                       {:status 200
                        :headers {"content-type" "text/html"}
                        :body (str 
                               (map :template @slave-cache))})))

(defn return-form-list [http-channel template-name]
  (let [template-set (filter (fn [x] (= (:template x) template-name)) @slave-cache)]
    (let [form-list 
          (interleave
           (map :name (:forms template-set))
           (map :content (:forms template-set)))]
      (enqueue http-channel {
                             :status 200 
                             :headers {"content-type" "text/plain"}
                             :body (str form-list)}))))
