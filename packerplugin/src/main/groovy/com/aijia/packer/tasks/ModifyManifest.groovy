package com.aijia.packer.tasks

import com.aijia.packer.util.LogUtil
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction


class ModifyManifest extends DefaultTask{
    @Input
    def File manifestFile

    @Input
    def manifestMatcher

    @Input
    def flavorName

    ModifyManifest() {
        setDescription('modify manifest meta-data value to channel name')
    }

    @TaskAction
    void showMessage() {
        LogUtil.i(project,"ModifyManifest-->${name}-->showMessage-->${description}")
    }

    @TaskAction
    void processMeta() {
        def temp = flavorName.split('-')
        if (temp && temp[0] && temp[1]) {
            def cn = temp[0].trim()
            def id = temp[1].trim()
            if (cn && id) {
                modifyManifest(cn, id)
            }
            /*if(temp[1]) {
                def id = temp[1].trim()
                if(id){ modifyJavaCode(id) }
            }*/
        }
    }

    void modifyManifest(def channelName, def channelId){
        LogUtil.i(project,"ModifyManifest-->${name}-->modifyManifest-->manifestFile:${manifestFile.absolutePath}")
        def root = new XmlSlurper().parse(manifestFile)
                .declareNamespace(android: "http://schemas.android.com/apk/res/android")
        LogUtil.i(project,"ModifyManifest-->${name}-->modifyManifest-->matcher:${manifestMatcher}")

        manifestMatcher?.each { String pattern ->
            if(pattern && "UMENG_CHANNEL".equals(pattern)){
                setChannelName(root,channelName,pattern)
            } else if(pattern && "XXSY_CHANNEL_ID".equals(pattern)){
                setChannelId(root,channelId,pattern)
            }
        }
        serializeXml(root,manifestFile)
    }

    /**设置渠道名*/
    void setChannelName(def root, def channelName, String pattern){
        def metadata = root.application.'meta-data'
        def found = metadata.find { mt ->
            pattern == mt.'@android:name'.toString()
        }
        if(found.size() > 0) {
            LogUtil.i(project,"${name}: ${pattern} found, modify it.")
            found.replaceNode {
                'meta-data'('android:name':found."@android:name", 'android:value': channelName){}
            }
        } else {
            LogUtil.i(project, "${name}: ${pattern} not found ,add it")
            root.application.appendNode {
                'meta-data'('android:name':pattern,'android:value': channelName) {}
            }
        }
    }

    /**设置渠道id*/
    void setChannelId(def root, def channelId, String pattern){
        def metadata = root.application.'meta-data'
        def found = metadata.find { mt ->
            pattern == mt.'@android:name'.toString()
        }
        if(found.size() > 0) {
            LogUtil.i(project,"${name}: ${pattern} found, modify it.")
            found.replaceNode {
                'meta-data'('android:name':found."@android:name", 'android:value': channelId){}
            }
        } else {
            LogUtil.i(project, "${name}: ${pattern} not found ,add it")
            root.application.appendNode {
                'meta-data'('android:name':pattern,'android:value': channelId) {}
            }
        }
    }

    void serializeXml(xml,file) {
        XmlUtil.serialize(new StreamingMarkupBuilder().bind {mkp.yield xml},new FileWriter(file))
    }
}