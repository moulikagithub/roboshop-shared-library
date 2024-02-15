def call(Map configMap) {
    pipeline {
        agent {
            node {
                label 'agent-1'
            }
        }
        environment { 
            packageVersion = ''
            // can maintain in pipeline globals
            //nexusURL = '172.31.5.95:8081'
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
            stage('Get the version') {
                steps {
                    script {
                        def packageJson = readJSON file: 'package.json'
                        packageVersion = packageJson.version
                        echo "application version: $packageVersion"
                    }
                }
            }    
            stage('install dependencies') {
                steps {
                  sh """
                   npm install
                  """
                }
            }
            stage('unit test') {
                steps {
                  sh """
                   echo "unit test cases will run here"
                  """
                }
            } 
            stage('sonarscan') {
                steps {
                  sh """
                   echo "this is the command for sonar scanning:[sonar-scanner]"
                   """
                }
            }
    
            stage('build') {
                steps {
                  sh """
                   ls -la
                   zip  -q -r catalogue.zip ./* -x ".git" -x "*.zip"
                   ls -ltr
                  """
                }
            }
        
            stage('publish Artifacts') {
                steps {
                  nexusArtifactUploader(
                    nexusVersion: 'nexus3',
                    protocol: 'http',
                    nexusUrl: pipelineGlobals.nexusURL(),
                    groupId: 'com.roboshop',
                    version: "${packageVersion}",
                    repository: "${configMap.component}",
                    credentialsId: 'nexus-auth',
                    artifacts: [
                         [artifactId: "${configMap.component}",
                         classifier: '',
                         file: "${configMap.component}.zip",
                         type: 'zip']
                    ]
                  )  
     
                }
            }
            stage('Deploy') {
              when {
                expression{
                    params.deploy == 'true'
                }
              }
              steps {
                script {
                    def params = [
                        string(name: 'version', value: "${packageVersion}"),
                        string(name: 'environment', value: "dev")
                    ]

                        build job: "../${configMap.component}-deploy", wait: true ,parameters: params

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
                echo 'this runs when pipeline is failed, used generally to send some alerts'
            }
            success{
                echo 'I will say Hello when pipeline is success'
            }
        }
        
    }
}