library "jenkinsci-unstashParam-library"

pipeline {
    agent any
    parameters {
        file(name: 'FileName', description: '上传Apple开发者后台创建的.mobileprovisioning结尾的文件，文件名中不允许带空格，否则会报错！')
    }
    
    stages {
        stage('FileManage') {
            steps {
                script {
                    // 清空工作区
                    cleanWs()

                    // 创建Provisioning Profiles文件夹
                    sh """
                    if [ ! -d "\$HOME/Library/MobileDevice/Provisioning Profiles" ]; then
                        mkdir -p "\$HOME/Library/MobileDevice/Provisioning Profiles"
                    fi
                    """

                    def file_in_workspace = unstashParam "FileName"
                    echo file_in_workspace
                    
                    // 获取描述文件中UUID
                    String uuid = sh(script:"""grep UUID -A1 -a ${file_in_workspace} |  grep -io '[-A-F0-9]\\{36\\}'""", returnStdout: true).trim()
                    echo uuid
                    
                    String fileName = uuid + '.mobileprovision'
                    sh """cp ${file_in_workspace} ~/Library/MobileDevice/'Provisioning Profiles'/'${fileName}'"""
                }
            }
        }
    }
}

