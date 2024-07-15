#!/usr/bin/octave -qf
clear;

oldpwd=pwd;
cd /home/nicomariv/FullBNT-1.0.4Octave/FullBNT-1.0.4;
addpath(genpath(pwd));
cd(oldpwd);
warning off;

pkg load io

node = {
	'stationComputer_shell','IED1_DERfailureOR','MMSclient_measCoher','historianServer_shellExecution','workStation_compromise','historianServer_NodeOR1','MMSclient_unsecCred','historian_scanVuln','MMSclient_spoofRepMsg','historian_remoteSrvc','virtualEng_suspiciousCmd','DMZ_scanIP','historianServer_remoteShell','MMSclient_fileAccess','historianServer_remoteShellOR','MMSTLS1_cmdCoher','IED1_DERfailure','MMSclient_modAuthProc','tomcatWebServer_loginFrequency','MMSTLS1_unauthCmdMsg','historianServer_addSSHkey','MMSclient_AND52','virtualEng_escapeHost','historianServer_rmtShellSession','MMSclient_credAcc','stationComputer_shellExecution','tomcatWebServer_bruteForce','historianServer_shell','MMSclient_integrityCheck','MMSTLS1_AITM'
};

names=[node];

n=length(names);


intrac = {
	'MMSTLS1_unauthCmdMsg','IED1_DERfailureOR';
	'workStation_compromise','DMZ_scanIP';
	'virtualEng_escapeHost','stationComputer_shell';
	'MMSTLS1_AITM','MMSTLS1_unauthCmdMsg';
	'historian_scanVuln','historian_remoteSrvc';
	'MMSTLS1_AITM','MMSclient_spoofRepMsg';
	'historian_remoteSrvc','historianServer_shell';
	'historianServer_addSSHkey','historianServer_remoteShellOR';
	'historianServer_shell','historianServer_addSSHkey';
	'MMSclient_modAuthProc','MMSclient_integrityCheck';
	'MMSclient_spoofRepMsg','MMSclient_measCoher';
	'MMSclient_unsecCred','MMSclient_fileAccess';
	'historianServer_NodeOR1','tomcatWebServer_bruteForce';
	'virtualEng_escapeHost','virtualEng_suspiciousCmd';
	'MMSclient_spoofRepMsg','IED1_DERfailureOR';
	'tomcatWebServer_bruteForce','tomcatWebServer_loginFrequency';
	'historianServer_remoteShellOR','historianServer_remoteShell';
	'MMSclient_credAcc','MMSTLS1_AITM';
	'historianServer_remoteShell','historianServer_rmtShellSession';
	'stationComputer_shell','MMSclient_modAuthProc';
	'MMSclient_AND52','MMSclient_credAcc';
	'MMSclient_modAuthProc','MMSclient_AND52';
	'MMSclient_unsecCred','MMSclient_AND52';
	'MMSTLS1_unauthCmdMsg','MMSTLS1_cmdCoher';
	'stationComputer_shell','MMSclient_unsecCred';
	'DMZ_scanIP','historian_scanVuln';
	'stationComputer_shell','stationComputer_shellExecution';
	'historianServer_shell','historianServer_shellExecution';
	'IED1_DERfailureOR','IED1_DERfailure';
	'historianServer_remoteShell','historianServer_NodeOR1';
	'historianServer_shell','historianServer_NodeOR1';
	'tomcatWebServer_bruteForce','virtualEng_escapeHost';
	'workStation_compromise','historianServer_remoteShellOR';
};

interc = {
	'IED1_DERfailure','IED1_DERfailure';
	'historianServer_remoteShell','historianServer_remoteShell';
	'DMZ_scanIP','DMZ_scanIP';
	'historian_remoteSrvc','historian_remoteSrvc';
	'historian_scanVuln','historian_scanVuln';
	'MMSclient_spoofRepMsg','MMSclient_spoofRepMsg';
	'MMSclient_unsecCred','MMSclient_unsecCred';
	'workStation_compromise','workStation_compromise';
	'stationComputer_shell','stationComputer_shell';
	'MMSTLS1_AITM','MMSTLS1_AITM';
	'tomcatWebServer_bruteForce','tomcatWebServer_bruteForce';
	'historianServer_shell','historianServer_shell';
	'MMSclient_credAcc','MMSclient_credAcc';
	'virtualEng_escapeHost','virtualEng_escapeHost';
	'MMSTLS1_unauthCmdMsg','MMSTLS1_unauthCmdMsg';
	'historianServer_addSSHkey','historianServer_addSSHkey';
	'MMSclient_modAuthProc','MMSclient_modAuthProc';
};

[intra, names] = mk_adj_mat(intrac, names, 1);
inter = mk_adj_mat(interc, names, 0);
ns = [2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2];

bnet = mk_dbn(intra, inter, ns, 'names', names);

stationComputer_shell=bnet.names('stationComputer_shell');

IED1_DERfailureOR=bnet.names('IED1_DERfailureOR');

MMSclient_measCoher=bnet.names('MMSclient_measCoher');

historianServer_shellExecution=bnet.names('historianServer_shellExecution');

workStation_compromise=bnet.names('workStation_compromise');

historianServer_NodeOR1=bnet.names('historianServer_NodeOR1');

MMSclient_unsecCred=bnet.names('MMSclient_unsecCred');

historian_scanVuln=bnet.names('historian_scanVuln');

MMSclient_spoofRepMsg=bnet.names('MMSclient_spoofRepMsg');

historian_remoteSrvc=bnet.names('historian_remoteSrvc');

virtualEng_suspiciousCmd=bnet.names('virtualEng_suspiciousCmd');

DMZ_scanIP=bnet.names('DMZ_scanIP');

historianServer_remoteShell=bnet.names('historianServer_remoteShell');

MMSclient_fileAccess=bnet.names('MMSclient_fileAccess');

historianServer_remoteShellOR=bnet.names('historianServer_remoteShellOR');

MMSTLS1_cmdCoher=bnet.names('MMSTLS1_cmdCoher');

IED1_DERfailure=bnet.names('IED1_DERfailure');

MMSclient_modAuthProc=bnet.names('MMSclient_modAuthProc');

tomcatWebServer_loginFrequency=bnet.names('tomcatWebServer_loginFrequency');

MMSTLS1_unauthCmdMsg=bnet.names('MMSTLS1_unauthCmdMsg');

historianServer_addSSHkey=bnet.names('historianServer_addSSHkey');

MMSclient_AND52=bnet.names('MMSclient_AND52');

virtualEng_escapeHost=bnet.names('virtualEng_escapeHost');

historianServer_rmtShellSession=bnet.names('historianServer_rmtShellSession');

MMSclient_credAcc=bnet.names('MMSclient_credAcc');

stationComputer_shellExecution=bnet.names('stationComputer_shellExecution');

tomcatWebServer_bruteForce=bnet.names('tomcatWebServer_bruteForce');

historianServer_shell=bnet.names('historianServer_shell');

MMSclient_integrityCheck=bnet.names('MMSclient_integrityCheck');

MMSTLS1_AITM=bnet.names('MMSTLS1_AITM');

%%%%%%%%% ------- slice 1 --------

%node stationComputer_shell(id=stationComputer_shell) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('stationComputer_shell')}=tabular_CPD(bnet,bnet.names('stationComputer_shell'),'CPT',cpt);
clear cpt;

%node IED1_DERfailureOR(id=IED1_DERfailureOR) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('IED1_DERfailureOR')}=tabular_CPD(bnet,bnet.names('IED1_DERfailureOR'),'CPT',cpt);
clear cpt;

%node MMSclient_measCoher(id=MMSclient_measCoher) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('MMSclient_measCoher')}=tabular_CPD(bnet,bnet.names('MMSclient_measCoher'),'CPT',cpt);
clear cpt;

%node historianServer_shellExecution(id=historianServer_shellExecution) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('historianServer_shellExecution')}=tabular_CPD(bnet,bnet.names('historianServer_shellExecution'),'CPT',cpt);
clear cpt;

%node workStation_compromise(id=workStation_compromise) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('workStation_compromise')}=tabular_CPD(bnet,bnet.names('workStation_compromise'),'CPT',cpt);
clear cpt;

%node historianServer_NodeOR1(id=historianServer_NodeOR1) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('historianServer_NodeOR1')}=tabular_CPD(bnet,bnet.names('historianServer_NodeOR1'),'CPT',cpt);
clear cpt;

%node MMSclient_unsecCred(id=MMSclient_unsecCred) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('MMSclient_unsecCred')}=tabular_CPD(bnet,bnet.names('MMSclient_unsecCred'),'CPT',cpt);
clear cpt;

%node historian_scanVuln(id=historian_scanVuln) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('historian_scanVuln')}=tabular_CPD(bnet,bnet.names('historian_scanVuln'),'CPT',cpt);
clear cpt;

%node MMSclient_spoofRepMsg(id=MMSclient_spoofRepMsg) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('MMSclient_spoofRepMsg')}=tabular_CPD(bnet,bnet.names('MMSclient_spoofRepMsg'),'CPT',cpt);
clear cpt;

%node historian_remoteSrvc(id=historian_remoteSrvc) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('historian_remoteSrvc')}=tabular_CPD(bnet,bnet.names('historian_remoteSrvc'),'CPT',cpt);
clear cpt;

%node virtualEng_suspiciousCmd(id=virtualEng_suspiciousCmd) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('virtualEng_suspiciousCmd')}=tabular_CPD(bnet,bnet.names('virtualEng_suspiciousCmd'),'CPT',cpt);
clear cpt;

%node DMZ_scanIP(id=DMZ_scanIP) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('DMZ_scanIP')}=tabular_CPD(bnet,bnet.names('DMZ_scanIP'),'CPT',cpt);
clear cpt;

%node historianServer_remoteShell(id=historianServer_remoteShell) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('historianServer_remoteShell')}=tabular_CPD(bnet,bnet.names('historianServer_remoteShell'),'CPT',cpt);
clear cpt;

%node MMSclient_fileAccess(id=MMSclient_fileAccess) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('MMSclient_fileAccess')}=tabular_CPD(bnet,bnet.names('MMSclient_fileAccess'),'CPT',cpt);
clear cpt;

%node historianServer_remoteShellOR(id=historianServer_remoteShellOR) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('historianServer_remoteShellOR')}=tabular_CPD(bnet,bnet.names('historianServer_remoteShellOR'),'CPT',cpt);
clear cpt;

%node MMSTLS1_cmdCoher(id=MMSTLS1_cmdCoher) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('MMSTLS1_cmdCoher')}=tabular_CPD(bnet,bnet.names('MMSTLS1_cmdCoher'),'CPT',cpt);
clear cpt;

%node IED1_DERfailure(id=IED1_DERfailure) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('IED1_DERfailure')}=tabular_CPD(bnet,bnet.names('IED1_DERfailure'),'CPT',cpt);
clear cpt;

%node MMSclient_modAuthProc(id=MMSclient_modAuthProc) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('MMSclient_modAuthProc')}=tabular_CPD(bnet,bnet.names('MMSclient_modAuthProc'),'CPT',cpt);
clear cpt;

%node tomcatWebServer_loginFrequency(id=tomcatWebServer_loginFrequency) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('tomcatWebServer_loginFrequency')}=tabular_CPD(bnet,bnet.names('tomcatWebServer_loginFrequency'),'CPT',cpt);
clear cpt;

%node MMSTLS1_unauthCmdMsg(id=MMSTLS1_unauthCmdMsg) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('MMSTLS1_unauthCmdMsg')}=tabular_CPD(bnet,bnet.names('MMSTLS1_unauthCmdMsg'),'CPT',cpt);
clear cpt;

%node historianServer_addSSHkey(id=historianServer_addSSHkey) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('historianServer_addSSHkey')}=tabular_CPD(bnet,bnet.names('historianServer_addSSHkey'),'CPT',cpt);
clear cpt;

%node MMSclient_AND52(id=MMSclient_AND52) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('MMSclient_AND52')}=tabular_CPD(bnet,bnet.names('MMSclient_AND52'),'CPT',cpt);
clear cpt;

%node virtualEng_escapeHost(id=virtualEng_escapeHost) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('virtualEng_escapeHost')}=tabular_CPD(bnet,bnet.names('virtualEng_escapeHost'),'CPT',cpt);
clear cpt;

%node historianServer_rmtShellSession(id=historianServer_rmtShellSession) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('historianServer_rmtShellSession')}=tabular_CPD(bnet,bnet.names('historianServer_rmtShellSession'),'CPT',cpt);
clear cpt;

%node MMSclient_credAcc(id=MMSclient_credAcc) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('MMSclient_credAcc')}=tabular_CPD(bnet,bnet.names('MMSclient_credAcc'),'CPT',cpt);
clear cpt;

%node stationComputer_shellExecution(id=stationComputer_shellExecution) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('stationComputer_shellExecution')}=tabular_CPD(bnet,bnet.names('stationComputer_shellExecution'),'CPT',cpt);
clear cpt;

%node tomcatWebServer_bruteForce(id=tomcatWebServer_bruteForce) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('tomcatWebServer_bruteForce')}=tabular_CPD(bnet,bnet.names('tomcatWebServer_bruteForce'),'CPT',cpt);
clear cpt;

%node historianServer_shell(id=historianServer_shell) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('historianServer_shell')}=tabular_CPD(bnet,bnet.names('historianServer_shell'),'CPT',cpt);
clear cpt;

%node MMSclient_integrityCheck(id=MMSclient_integrityCheck) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('MMSclient_integrityCheck')}=tabular_CPD(bnet,bnet.names('MMSclient_integrityCheck'),'CPT',cpt);
clear cpt;

%node MMSTLS1_AITM(id=MMSTLS1_AITM) slice 1 
%parent order:{}
cpt(:,:)=[1.0, 0.0];
bnet.CPD{bnet.names('MMSTLS1_AITM')}=tabular_CPD(bnet,bnet.names('MMSTLS1_AITM'),'CPT',cpt);
clear cpt;


%%%%%%%%% ------- slice 2 --------

%node IED1_DERfailure(id=IED1_DERfailure) slice 2 
%parent order:{IED1_DERfailure,}
cpt(1,1,:)=[, 0];
cpt(1,2,:)=[0, 0];
cpt(2,1,:)=[0, 0];
cpt(2,2,:)=[0, 0];
cpt1=mk_named_CPT_inter({'IED1_DERfailure','', 'IED1_DERfailure'},names, bnet.dag, cpt, [2]);
bnet.CPD{bnet.eclass2(bnet.names('IED1_DERfailure'))}=tabular_CPD(bnet,n+bnet.names('IED1_DERfailure'),'CPT',cpt1);
clear cpt;clear cpt1;

%node historianServer_remoteShell(id=historianServer_remoteShell) slice 2 
%parent order:{historianServer_remoteShell,}
cpt(1,1,:)=[, 0];
cpt(1,2,:)=[0, 0];
cpt(2,1,:)=[0, 0];
cpt(2,2,:)=[0, 0];
cpt1=mk_named_CPT_inter({'historianServer_remoteShell','', 'historianServer_remoteShell'},names, bnet.dag, cpt, [2]);
bnet.CPD{bnet.eclass2(bnet.names('historianServer_remoteShell'))}=tabular_CPD(bnet,n+bnet.names('historianServer_remoteShell'),'CPT',cpt1);
clear cpt;clear cpt1;

%node DMZ_scanIP(id=DMZ_scanIP) slice 2 
%parent order:{DMZ_scanIP,}
cpt(1,1,:)=[, 0];
cpt(1,2,:)=[0, 0];
cpt(2,1,:)=[0, 0];
cpt(2,2,:)=[0, 0];
cpt1=mk_named_CPT_inter({'DMZ_scanIP','', 'DMZ_scanIP'},names, bnet.dag, cpt, [2]);
bnet.CPD{bnet.eclass2(bnet.names('DMZ_scanIP'))}=tabular_CPD(bnet,n+bnet.names('DMZ_scanIP'),'CPT',cpt1);
clear cpt;clear cpt1;

%node historian_remoteSrvc(id=historian_remoteSrvc) slice 2 
%parent order:{historian_remoteSrvc,}
cpt(1,1,:)=[, 0];
cpt(1,2,:)=[0, 0];
cpt(2,1,:)=[0, 0];
cpt(2,2,:)=[0, 0];
cpt1=mk_named_CPT_inter({'historian_remoteSrvc','', 'historian_remoteSrvc'},names, bnet.dag, cpt, [2]);
bnet.CPD{bnet.eclass2(bnet.names('historian_remoteSrvc'))}=tabular_CPD(bnet,n+bnet.names('historian_remoteSrvc'),'CPT',cpt1);
clear cpt;clear cpt1;

%node historian_scanVuln(id=historian_scanVuln) slice 2 
%parent order:{historian_scanVuln,}
cpt(1,1,:)=[, 0];
cpt(1,2,:)=[0, 0];
cpt(2,1,:)=[0, 0];
cpt(2,2,:)=[0, 0];
cpt1=mk_named_CPT_inter({'historian_scanVuln','', 'historian_scanVuln'},names, bnet.dag, cpt, [2]);
bnet.CPD{bnet.eclass2(bnet.names('historian_scanVuln'))}=tabular_CPD(bnet,n+bnet.names('historian_scanVuln'),'CPT',cpt1);
clear cpt;clear cpt1;

%node MMSclient_spoofRepMsg(id=MMSclient_spoofRepMsg) slice 2 
%parent order:{MMSclient_spoofRepMsg,}
cpt(1,1,:)=[, 0];
cpt(1,2,:)=[0, 0];
cpt(2,1,:)=[0, 0];
cpt(2,2,:)=[0, 0];
cpt1=mk_named_CPT_inter({'MMSclient_spoofRepMsg','', 'MMSclient_spoofRepMsg'},names, bnet.dag, cpt, [2]);
bnet.CPD{bnet.eclass2(bnet.names('MMSclient_spoofRepMsg'))}=tabular_CPD(bnet,n+bnet.names('MMSclient_spoofRepMsg'),'CPT',cpt1);
clear cpt;clear cpt1;

%node MMSclient_unsecCred(id=MMSclient_unsecCred) slice 2 
%parent order:{MMSclient_unsecCred,}
cpt(1,1,:)=[, 0];
cpt(1,2,:)=[0, 0];
cpt(2,1,:)=[0, 0];
cpt(2,2,:)=[0, 0];
cpt1=mk_named_CPT_inter({'MMSclient_unsecCred','', 'MMSclient_unsecCred'},names, bnet.dag, cpt, [2]);
bnet.CPD{bnet.eclass2(bnet.names('MMSclient_unsecCred'))}=tabular_CPD(bnet,n+bnet.names('MMSclient_unsecCred'),'CPT',cpt1);
clear cpt;clear cpt1;

%node workStation_compromise(id=workStation_compromise) slice 2 
%parent order:{workStation_compromise,}
cpt(1,1,:)=[, 0];
cpt(1,2,:)=[0, 0];
cpt(2,1,:)=[0, 0];
cpt(2,2,:)=[0, 0];
cpt1=mk_named_CPT_inter({'workStation_compromise','', 'workStation_compromise'},names, bnet.dag, cpt, [2]);
bnet.CPD{bnet.eclass2(bnet.names('workStation_compromise'))}=tabular_CPD(bnet,n+bnet.names('workStation_compromise'),'CPT',cpt1);
clear cpt;clear cpt1;

%node stationComputer_shell(id=stationComputer_shell) slice 2 
%parent order:{stationComputer_shell,}
cpt(1,1,:)=[, 0];
cpt(1,2,:)=[0, 0];
cpt(2,1,:)=[0, 0];
cpt(2,2,:)=[0, 0];
cpt1=mk_named_CPT_inter({'stationComputer_shell','', 'stationComputer_shell'},names, bnet.dag, cpt, [2]);
bnet.CPD{bnet.eclass2(bnet.names('stationComputer_shell'))}=tabular_CPD(bnet,n+bnet.names('stationComputer_shell'),'CPT',cpt1);
clear cpt;clear cpt1;

%node MMSTLS1_AITM(id=MMSTLS1_AITM) slice 2 
%parent order:{MMSTLS1_AITM,}
cpt(1,1,:)=[, 0];
cpt(1,2,:)=[0, 0];
cpt(2,1,:)=[0, 0];
cpt(2,2,:)=[0, 0];
cpt1=mk_named_CPT_inter({'MMSTLS1_AITM','', 'MMSTLS1_AITM'},names, bnet.dag, cpt, [2]);
bnet.CPD{bnet.eclass2(bnet.names('MMSTLS1_AITM'))}=tabular_CPD(bnet,n+bnet.names('MMSTLS1_AITM'),'CPT',cpt1);
clear cpt;clear cpt1;

%node tomcatWebServer_bruteForce(id=tomcatWebServer_bruteForce) slice 2 
%parent order:{tomcatWebServer_bruteForce,}
cpt(1,1,:)=[, 0];
cpt(1,2,:)=[0, 0];
cpt(2,1,:)=[0, 0];
cpt(2,2,:)=[0, 0];
cpt1=mk_named_CPT_inter({'tomcatWebServer_bruteForce','', 'tomcatWebServer_bruteForce'},names, bnet.dag, cpt, [2]);
bnet.CPD{bnet.eclass2(bnet.names('tomcatWebServer_bruteForce'))}=tabular_CPD(bnet,n+bnet.names('tomcatWebServer_bruteForce'),'CPT',cpt1);
clear cpt;clear cpt1;

%node historianServer_shell(id=historianServer_shell) slice 2 
%parent order:{historianServer_shell,}
cpt(1,1,:)=[, 0];
cpt(1,2,:)=[0, 0];
cpt(2,1,:)=[0, 0];
cpt(2,2,:)=[0, 0];
cpt1=mk_named_CPT_inter({'historianServer_shell','', 'historianServer_shell'},names, bnet.dag, cpt, [2]);
bnet.CPD{bnet.eclass2(bnet.names('historianServer_shell'))}=tabular_CPD(bnet,n+bnet.names('historianServer_shell'),'CPT',cpt1);
clear cpt;clear cpt1;

%node MMSclient_credAcc(id=MMSclient_credAcc) slice 2 
%parent order:{MMSclient_credAcc,}
cpt(1,1,:)=[, 0];
cpt(1,2,:)=[0, 0];
cpt(2,1,:)=[0, 0];
cpt(2,2,:)=[0, 0];
cpt1=mk_named_CPT_inter({'MMSclient_credAcc','', 'MMSclient_credAcc'},names, bnet.dag, cpt, [2]);
bnet.CPD{bnet.eclass2(bnet.names('MMSclient_credAcc'))}=tabular_CPD(bnet,n+bnet.names('MMSclient_credAcc'),'CPT',cpt1);
clear cpt;clear cpt1;

%node virtualEng_escapeHost(id=virtualEng_escapeHost) slice 2 
%parent order:{virtualEng_escapeHost,}
cpt(1,1,:)=[, 0];
cpt(1,2,:)=[0, 0];
cpt(2,1,:)=[0, 0];
cpt(2,2,:)=[0, 0];
cpt1=mk_named_CPT_inter({'virtualEng_escapeHost','', 'virtualEng_escapeHost'},names, bnet.dag, cpt, [2]);
bnet.CPD{bnet.eclass2(bnet.names('virtualEng_escapeHost'))}=tabular_CPD(bnet,n+bnet.names('virtualEng_escapeHost'),'CPT',cpt1);
clear cpt;clear cpt1;

%node MMSTLS1_unauthCmdMsg(id=MMSTLS1_unauthCmdMsg) slice 2 
%parent order:{MMSTLS1_unauthCmdMsg,}
cpt(1,1,:)=[, 0];
cpt(1,2,:)=[0, 0];
cpt(2,1,:)=[0, 0];
cpt(2,2,:)=[0, 0];
cpt1=mk_named_CPT_inter({'MMSTLS1_unauthCmdMsg','', 'MMSTLS1_unauthCmdMsg'},names, bnet.dag, cpt, [2]);
bnet.CPD{bnet.eclass2(bnet.names('MMSTLS1_unauthCmdMsg'))}=tabular_CPD(bnet,n+bnet.names('MMSTLS1_unauthCmdMsg'),'CPT',cpt1);
clear cpt;clear cpt1;

%node historianServer_addSSHkey(id=historianServer_addSSHkey) slice 2 
%parent order:{historianServer_addSSHkey,}
cpt(1,1,:)=[, 0];
cpt(1,2,:)=[0, 0];
cpt(2,1,:)=[0, 0];
cpt(2,2,:)=[0, 0];
cpt1=mk_named_CPT_inter({'historianServer_addSSHkey','', 'historianServer_addSSHkey'},names, bnet.dag, cpt, [2]);
bnet.CPD{bnet.eclass2(bnet.names('historianServer_addSSHkey'))}=tabular_CPD(bnet,n+bnet.names('historianServer_addSSHkey'),'CPT',cpt1);
clear cpt;clear cpt1;

%node MMSclient_modAuthProc(id=MMSclient_modAuthProc) slice 2 
%parent order:{MMSclient_modAuthProc,}
cpt(1,1,:)=[, 0];
cpt(1,2,:)=[0, 0];
cpt(2,1,:)=[0, 0];
cpt(2,2,:)=[0, 0];
cpt1=mk_named_CPT_inter({'MMSclient_modAuthProc','', 'MMSclient_modAuthProc'},names, bnet.dag, cpt, [2]);
bnet.CPD{bnet.eclass2(bnet.names('MMSclient_modAuthProc'))}=tabular_CPD(bnet,n+bnet.names('MMSclient_modAuthProc'),'CPT',cpt1);
clear cpt;clear cpt1;


