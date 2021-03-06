package com.antell.cloudhands.api.packet.tcp.ftp;

import com.antell.cloudhands.api.packet.SessionEntry;
import com.antell.cloudhands.api.packet.tcp.TCPSessionEntry;
import com.antell.cloudhands.api.rule.RuleConstants;
import com.antell.cloudhands.api.rule.RuleItem;
import com.antell.cloudhands.api.rule.RuleUtils;
import com.antell.cloudhands.api.source.AbstractSourceEntry;
import com.antell.cloudhands.api.utils.*;
import com.google.common.base.Preconditions;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FTPSession extends AbstractSourceEntry {

    private SessionEntry sessionEntry;
    private String user;
    private String passwd;
    private int loginCode;
    private String statBruteForce;
    private List<FTPCmd> cmdList;

    public FTPSession(MessageUnpacker unpacker) throws IOException {

        this.sessionEntry = new TCPSessionEntry();
        this.user = "";
        this.passwd = "";
        this.loginCode = 0;
        this.statBruteForce = "";
        this.cmdList = new ArrayList<>();
        parse(unpacker);

        setLoginCode(0);
    }

    private void parse(MessageUnpacker unpacker) throws IOException {

        int n = MessagePackUtil.parseMapHeader(unpacker,false);
        Preconditions.checkArgument(n==2,"Invalid ftp session messagePack:"+n);

        /*parse session entry */
        sessionEntry.parse(unpacker);

        n = MessagePackUtil.parseMapHeader(unpacker,true);
        Preconditions.checkArgument(n==3,"Invalid ftp session messagePack:"+n);

        user = MessagePackUtil.parseText(unpacker);
        passwd = MessagePackUtil.parseText(unpacker);

        setStatBruteForce();

        n = MessagePackUtil.parseArrayHeader(unpacker,true);

        for(int i = 0;i<n;i++){

            cmdList.add(new FTPCmd(unpacker));
        }

    }


    public SessionEntry getSessionEntry() {
        return sessionEntry;
    }

    public void setSessionEntry(SessionEntry sessionEntry) {
        this.sessionEntry = sessionEntry;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public List<FTPCmd> getCmdList() {
        return cmdList;
    }

    public void setCmdList(List<FTPCmd> cmdList) {
        this.cmdList = cmdList;
    }

    public int getLoginCode() {
        return loginCode;
    }

    public void setLoginCode(int loginCode) {

        for(FTPCmd cmd:cmdList){

            if(cmd.getCmd().equalsIgnoreCase("PASS")){

                List<FTPAns> ansList = cmd.getAnsList();

                if(ansList!=null&&ansList.size()>0){

                    FTPAns ans = ansList.get(0);
                    this.loginCode = ans.getCode();
                }

                break;
            }
        }
    }

    public String getStatBruteForce() {
        return statBruteForce;
    }

    public void setStatBruteForce() {

        StringBuffer sb = new StringBuffer();
        sb.append(sessionEntry.getReqIP());
        sb.append("|");
        sb.append(sessionEntry.getResIP());
        sb.append("|");
        sb.append(user);
        
        this.statBruteForce = sb.toString();
    }

    private FTPCmd findCmd(String cmd){

        if(cmdList == null ||cmdList.size()==0)
            return null;

        for(FTPCmd ftpCmd:cmdList){

            String value = ftpCmd.getCmd();
            if(!TextUtils.isEmpty(value)){
                if(value.toLowerCase().equals(cmd.toLowerCase()))
                    return ftpCmd;
            }
        }
        return null;
    }

    private List<String> getCmds(){

        List<String> cmds = new ArrayList<>();
        if(cmdList!=null&&cmdList.size()>0){

            cmdList.forEach(ftpCmd -> {

                if(!TextUtils.isEmpty(ftpCmd.getCmd()))
                    cmds.add(ftpCmd.getCmd());
            });
        }

        return cmds;
    }

    private List<String> getCmdRes(String cmd,boolean isCode){

        List<String> res = new ArrayList<>();

        FTPCmd ftpCmd = findCmd(cmd);
        if(ftpCmd!=null){

            List<FTPAns> ans = ftpCmd.getAnsList();
            if(ans!=null&&ans.size()>0){

                ans.forEach(a->{
                    if(isCode)
                        res.add(String.format("%d",a.getCode()));
                    else
                    {
                        if(!TextUtils.isEmpty(a.getPhrase()))
                            res.add(a.getPhrase());
                    }
                });
            }
        }

        return res;
    }

    @Override
    public boolean canMatch(String proto) {
        return proto.equals(RuleConstants.ftp);
    }

    @Override
    public String getTargetValue(RuleItem ruleItem) {

        String target = ruleItem.getTarget();
        boolean isHex = ruleItem.isHex();

        if(target.equals(RuleConstants.ftpUser))
            return RuleUtils.targetValue(user,isHex);

        if(target.equals(RuleConstants.ftpPasswd))
            return RuleUtils.targetValue(passwd,isHex);

        if(target.equals(RuleConstants.ftpLoginCode))
            return RuleUtils.targetValue(loginCode,isHex);

        if(target.equals(RuleConstants.ftpCmds))
            return RuleUtils.targetValue(getCmds(),",",isHex);

        if(target.startsWith(RuleConstants.ftpCmdResCodes))
        {
            String[] splits = target.split("\\.");
            if(splits.length!=2)
                return "";

            return RuleUtils.targetValue(getCmdRes(splits[1],true),",",isHex);
        }
        if(target.startsWith(RuleConstants.ftpCmdResPhrase))
        {
            String[] splits = target.split("\\.");
            if(splits.length!=2)
                return "";

            return RuleUtils.targetValue(getCmdRes(splits[1],false),",",isHex);
        }

        if(target.startsWith(RuleConstants.ftpCmdArgs)){
            String[] splits = target.split("\\.");
            if(splits.length!=2)
                return "";

            FTPCmd ftpCmd = findCmd(splits[1]);
            if(ftpCmd!=null)
                return RuleUtils.targetValue(ftpCmd.getArgs(),isHex);
            return "";
        }

        return sessionEntry.getSessionTargetValue(ruleItem);
    }

    private class FTPCmd {

        private String cmd;
        private String args;
        private List<FTPAns> ansList;

        public FTPCmd(){

            cmd = "";
            args = "";
            ansList = new ArrayList<>();
        }

        public FTPCmd(MessageUnpacker unpacker) throws IOException {

            this();

            int n = MessagePackUtil.parseMapHeader(unpacker,true);
            Preconditions.checkArgument(n == 3,"Invalid ftp session cmd!");

            cmd = MessagePackUtil.parseText(unpacker);
            args = MessagePackUtil.parseText(unpacker);

            n = MessagePackUtil.parseArrayHeader(unpacker,true);
            
            for(int i = 0;i<n;i++){
                
                FTPAns ans = new FTPAns(unpacker);
                ansList.add(ans);
            }

        }

        public XContentBuilder toJson(XContentBuilder cb) throws IOException {

            XContentBuilder cbb = cb.startObject();

            cbb.field("cmd",TextUtils.getStrValue(cmd));
            cbb.field("args",TextUtils.getStrValue(args));
            XContentBuilder ansJson = cbb.startArray("ansList");

            ansList.forEach(ans-> {
                try {
                    ans.toJson(ansJson);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            ansJson.endArray();

            cbb.endObject();
            return cb;
        }

        public String toString(){

            StringBuffer sb = new StringBuffer();
            TextUtils.addText(sb,"cmd",cmd);
            TextUtils.addText(sb,"args",args);

            TextUtils.addText(sb,"ansList","");

            ansList.forEach(ans->sb.append(ans.toString()));

            return sb.toString();
        }

        public String getCmd() {
            return cmd;
        }

        public void setCmd(String cmd) {
            this.cmd = cmd;
        }

        public String getArgs() {
            return args;
        }

        public void setArgs(String args) {
            this.args = args;
        }

        public List<FTPAns> getAnsList() {
            return ansList;
        }

        public void setAnsList(List<FTPAns> ansList) {
            this.ansList = ansList;
        }
    }

    private class FTPAns{

        private int code;
        private String phrase;

        public FTPAns(){

            code = 0;
            phrase = "";
        }


        public FTPAns(MessageUnpacker unpacker) throws IOException {

            Preconditions.checkArgument(MessagePackUtil.parseMapHeader(unpacker,true)==2,"Invalid ftp ans!");

            code = MessagePackUtil.parseInt(unpacker);
            phrase = MessagePackUtil.parseText(unpacker);

        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getPhrase() {
            return phrase;
        }

        public void setPhrase(String phrase) {
            this.phrase = phrase;
        }

        XContentBuilder toJson(XContentBuilder cb) throws IOException {

            XContentBuilder cbb = cb.startObject();

            cbb.field("code",code);
            cbb.field("phrase",TextUtils.getStrValue(phrase));
            cbb.endObject();

            return cb;
        }

        public String toString(){

            StringBuffer sb = new StringBuffer();
            TextUtils.addInt(sb,"ans.code",code);
            TextUtils.addText(sb,"ans.phrase",phrase);

            return sb.toString();
        }

    }


    @Override
    public String dataToString() {

        StringBuffer sb = new StringBuffer();
        sb.append(sessionEntry.dataToString());
        TextUtils.addText(sb,"user",user);
        TextUtils.addText(sb,"passwd",passwd);
        TextUtils.addInt(sb,"loginCode",loginCode);
        TextUtils.addText(sb,"statBruteForce",statBruteForce);
        TextUtils.addText(sb,"cmdList","");
        cmdList.forEach(cmd->sb.append(cmd.toString()));

        return sb.toString();

    }

    public String toString(){

        return dataToString();
    }

    public void ansListToStringBuilder(StringBuffer sb, List<FTPAns> ansList) {
        sb.append("\"ans\":[");
        for (Iterator<FTPAns> iterator = ansList.iterator(); iterator.hasNext();) {
            final FTPAns ftpAns = iterator.next();
            sb.append("\"").append(ftpAns.getCode()).append(" ").append(ftpAns.getPhrase()).append("\"");
            if (iterator.hasNext()) {
                sb.append(",");
            }
        }
        sb.append("]");
    }
    public String cmdListToString(List<FTPCmd> cmdList){
        final StringBuffer sb = new StringBuffer("[");
        for (Iterator<FTPCmd> iterator = cmdList.iterator(); iterator.hasNext();) {
            final FTPCmd ftpCmd = iterator.next();
            sb.append("{");
            TextUtils.addText(sb, "cmd", ftpCmd.getCmd() + " " + ftpCmd.getArgs(), true);
            ansListToStringBuilder(sb, ftpCmd.getAnsList());
            sb.append("}");
            if (iterator.hasNext()) {
                sb.append(",");
            }
        }
        return sb.append("]").toString();
    }

    @Override
    public XContentBuilder dataToJson(XContentBuilder cb) throws IOException {

        XContentBuilder seCB = cb.startObject("sessionEntry");
        sessionEntry.dataToJson(seCB);
        seCB.endObject();

        cb.field("user",TextUtils.getStrValue(user));
        cb.field("passwd",TextUtils.getStrValue(passwd));
        cb.field("loginCode",loginCode);
        cb.field("statBruteForce",statBruteForce);

        XContentBuilder cbb = cb.startArray("cmdList");

        cmdList.forEach(cmd-> {
            try {
                cmd.toJson(cbb);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        cbb.endArray();

        return cb;
    }

    @Override
    public String getIndexMapping() {

        String mapping = "{" +
                "\"properties\":{" +
                "\"id\":{\"type\":\"keyword\"}," +
                "\"sessionEntry\":{" +
                "\"properties\":{" +
                "\"sessionID\":{\"type\":\"long\"}," +
                "\"protocol\":{\"type\":\"keyword\"}," +
                "\"srcIP\":{\"type\":\"keyword\"}," +
                "\"dstIP\":{\"type\":\"keyword\"}," +
                "\"srcPort\":{\"type\":\"integer\"}," +
                "\"dstPort\":{\"type\":\"integer\"}," +
                "\"reqStartTime\":{\"type\":\"long\"}," +
                "\"resStartTime\":{\"type\":\"long\"}," +
                "\"timeDate\":{\"type\":\"date\",\"format\":\"yyyy-MM-dd HH:mm:ss\"}," +
                "\"reqPackets\":{\"type\":\"long\"}," +
                "\"reqBytes\":{\"type\":\"long\"}," +
                "\"reqPBytes\":{\"type\":\"long\"}," +
                "\"resPackets\":{\"type\":\"long\"}," +
                "\"resBytes\":{\"type\":\"long\"}," +
                "\"resPBytes\":{\"type\":\"long\"}" +
                "}" +
                "}," +
                "\"user\":{\"type\":\"keyword\"}," +
                "\"passwd\":{\"type\":\"keyword\"}," +
                "\"statBruteForce\":{\"type\":\"keyword\"}," +
                "\"srcIPLocation\":{" +
                "\"properties\":{" +
                "\"location\":{\"type\":\"keyword\"}," +
                "\"country\":{\"type\":\"keyword\"}," +
                "\"city\":{\"type\":\"keyword\"}," +
                "\"longitude\":{\"type\":\"double\"}," +
                "\"latitude\":{\"type\":\"double\"}" +
                "}" +
                "}," +
                "\"dstIPLocation\":{" +
                "\"properties\":{" +
                "\"location\":{\"type\":\"keyword\"}," +
                "\"country\":{\"type\":\"keyword\"}," +
                "\"city\":{\"type\":\"keyword\"}," +
                "\"longitude\":{\"type\":\"double\"}," +
                "\"latitude\":{\"type\":\"double\"}" +
                "}" +
                "}" +
                "}" +
                "}";
        return mapping;
    }

    @Override
    public String getIndexNamePrefix() {
        return "log_tcp_session_ftp";
    }

    @Override
    public String getIndexDocType() {
        return Constants.ESLOGDOCTYPE;
    }

    @Override
    public String getSrcIP() {
        return IPUtils.ipv4Str(sessionEntry.getReqIP());
    }

    @Override
    public String getDstIP() {
        return IPUtils.ipv4Str(sessionEntry.getResIP());
    }

    public String getProto(){

        return "ftp";
    }

    public long getSrcIPI(){

        return sessionEntry.getReqIP();
    }

    public long getDstIPI(){
        return sessionEntry.getResIP();
    }

    public long getTime(){

        return sessionEntry.getReqStartTime();
    }

}
