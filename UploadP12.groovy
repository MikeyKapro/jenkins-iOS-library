library "jenkinsci-unstashParam-library"

pipeline {
    agent any
    parameters {
        file(name: 'FileName', description: '上传Apple开发者后台创建的.p12结尾的文件，文件名中不允许带空格，否则会报错！')
        string(name: 'Password', defaultValue: "''", description: "p12证书的密码，空密码时请填''")
        string(name: 'PowerOnPassword', defaultValue: '', description: '开机密码')
    }
    
    stages {
        stage('FileManage') {
            steps {
                script {
                    // 清空工作区
                    cleanWs()

                    def file_in_workspace = unstashParam "FileName"
                    echo file_in_workspace
                    
                    // TODO: 查找传入证书的SHA-256或SHA-1，删除原有的identity，因为授权的应用程序无法覆盖原有授权的应用程序
                    // 删除过期和被撤销的证书
                    String shaString = sh(script:"""security find-identity -p codesigning ~/Library/Keychains/login.keychain |egrep 'CSSMERR_TP_CERT_EXPIRED|CSSMERR_TP_CERT_REVOKED' | awk '{print \$2}'""", returnStdout: true).trim()
                    String[] shas = shaString.split('\n')
                    for(String sha : shas)
                        if (sha.length() == 0) continue

                        try {
                            sh """
                                security delete-identity -Z ${sha}
                            """
                        }catch(Exception e) {}
                    
                    sh(script:"""security unlock-keychain -p ${env.PowerOnPassword} ~/Library/Keychains/login.keychain""", returnStdout: true).trim()
                    // 安装p12证书，并授权codesign永久访问权限
                    sh(script:"""security import ${file_in_workspace} -k ~/Library/Keychains/login.keychain -P ${env.Password} -T /usr/bin/codesign""", returnStdout: true).trim()
                    // 访问授权
                    sh(script:"""security set-key-partition-list -S apple-tool:,apple: -s -k ${env.PowerOnPassword} ~/Library/Keychains/login.keychain""", returnStdout: true).trim()
                }
            }
        }
    }
}

