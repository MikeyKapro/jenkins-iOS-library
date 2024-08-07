library "jenkinsci-unstashParam-library"
pipeline {
    agent any
    parameters {
        string(name: 'ProjectPath', defaultValue: 'oss://', description: 'Bucket下存储IPA包的路径，以/结尾，如oss://examplebucket/dir/')
        file(name: 'icon57x57', description: '57x57的图片')
        file(name: 'icon512x512', description: '512x512的图片')
    }
    
    stages {
        stage('Create Path') {
            steps {
                script {
                    // 创建主路径
                    try {
                        sh """ossutil mkdir ${params.ProjectPath}"""
                    }
                    catch(Exception e) {}
                    
                    // 上传图片
                    def icon_57_57 = unstashParam "icon57x57"
                    def icon_512_512 = unstashParam "icon512x512"
                    sh """ossutil cp ${icon_57_57} ${ProjectPath} -f"""
                    sh """ossutil cp ${icon_512_512} ${ProjectPath} -f"""
                }
            }
        }
    }
}
