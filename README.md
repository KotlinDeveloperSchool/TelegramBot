# TelegramBot


## Диаграммы
- Диаграмма отношений: `./EntityBD.puml` [Рендер](http://www.plantuml.com/plantuml/uml/dPF1Jjj048RlVeeHbqA4CJrmYGAdA9TUgaWebqgLPM-I5SuwjTUbKK4gGIgjIiMnnnvxTH4M4P3m2hjlr7yt2OvAKXKKnVREplpp_UFrYwwK_RJgUVt85pBhHLj1tFE2o4zJD-H-9_5xJfU7Z0AU115pqWCk-ZwY5771vA3FA4vaDtOS9YIN0rfhnKcuHdvABVhe4F6GgiI5917ZdqKHrMeT9bZYInRkxT4HLIglNrKgI7LTN48UoBVBYhW8sOTPGMEX8CbI90TTFrdVsTvugC0E4vsbEySbR1tTU2pyP62Hwyz2ZW-nzoHcuVVO2hGM7SUkUtH4zP9oFsM9vTj_5f_iB87pl4pmTndpl98JkHbQBTAztDxUGBZDarGkYUESF0uVE_MvYVtIHCD_7oTSCckaOOrOEjFnVuUtud0iPEpkuj9FU0-lb0xOW691wxCMc_DwW-WqIiR6afatMPf5qi8sdpNrGtm7_sPTuVn9VcshD0i2bgRJDFEsV1wLgv2tsmyBEvzojYkdWrwCPDodk4tDAgcVgj2VrAMQgB7AzPdArQq-n_E4S2b8_TQdwaeLwkwDG5vXzjL8NIF_Kdz3NQw7-dmc6x6sdEfM3Hk0r8-PR87x27hGoFK9VcTgX4gh088X8XUaRh2ueVMfJNTktvn7CDoQZB75pL46H7LdLtX2-L2Dh3gMLtOhtoBrov3310AmfR-e0bhwEvBXx_fljg7DDaFGNnUywo5Pi_SGlveQ9ds2EKpCt3ODNKxg7ir7qtTIsoLtX-Oc7rLODNqwwpo6yftH69d96YguqnVcRkgcWndFEqpqPrC5LUE_CAeJ1EtIs9jQmM4FpQbYZjCiluQ7nvRp1m00)

## Подготовка к запуску
1. Поднять БД: выполнить скрипт `docker/run-db.sh`
  В docker будут подняты контейнеры: PostgreSQL на порту 5432 с БД **bot** и [Adminer](http://localhost:8081/))
2. При старте приложения Liquid проверит и подготовит базу `src/main/resources/db/changelog/db.changelog-master.yaml`
