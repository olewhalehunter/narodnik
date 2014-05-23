(defun lein-server () (interactive)
  (async-shell-command "lein ring server")
  )
(defun lein-run () (interactive)
  (async-shell-command "lein run")
  )



(global-set-key (kbd "C-2") 'lein-server)
(global-set-key (kbd "C-3") 'lein-run)
