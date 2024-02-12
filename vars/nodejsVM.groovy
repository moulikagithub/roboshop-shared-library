pipeline {
    agent {
        node {
            label 'agent-1'
        }      
    }
    environment { 
            packageVersion = ''
            nexusurl = '172.31.11.253:8081'
    }
   
    options {
                timeout(time: 1, unit: 'HOURS')
                disableConcurrentBuilds() 
            }
    parameters {
        
        booleanParam(name: 'deploy', defaultValue: false, description: 'Toggle this value')
    }
    // build
    stages {
        stage('get version') {
            steps {
                script {
                    def packageJson = readJSON file: 'package.json'
                    packageVersion = packageJson.version
                    echo "application version: $packageVersion"
                }
            }
        }  
    }
    // post build
    post { 
        always { 
            echo 'I will always say Hello again!'
            deleteDir()
        }
        failure { 
            echo 'runs when their is failure'
        }
        success { 
            echo 'runs when success!'
        }
    }
}