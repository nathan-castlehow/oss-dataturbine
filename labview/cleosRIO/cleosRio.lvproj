<?xml version='1.0'?>
<Project Type="Project" LVVersion="8508002">
   <Item Name="My Computer" Type="My Computer">
      <Property Name="server.app.propertiesEnabled" Type="Bool">true</Property>
      <Property Name="server.control.propertiesEnabled" Type="Bool">true</Property>
      <Property Name="server.tcp.enabled" Type="Bool">false</Property>
      <Property Name="server.tcp.port" Type="Int">0</Property>
      <Property Name="server.tcp.serviceName" Type="Str">My Computer/VI Server</Property>
      <Property Name="server.tcp.serviceName.default" Type="Str">My Computer/VI Server</Property>
      <Property Name="server.vi.callsEnabled" Type="Bool">true</Property>
      <Property Name="server.vi.propertiesEnabled" Type="Bool">true</Property>
      <Property Name="specify.custom.address" Type="Bool">false</Property>
      <Item Name="Dependencies" Type="Dependencies"/>
      <Item Name="Build Specifications" Type="Build"/>
   </Item>
   <Item Name="SMER" Type="RT CompactRIO">
      <Property Name="alias.name" Type="Str">SMER</Property>
      <Property Name="alias.value" Type="Str">172.23.37.61</Property>
      <Property Name="CCSymbols" Type="Str">TARGET_TYPE,RT;OS,PharLap;CPU,x86;</Property>
      <Property Name="crio.family" Type="Str">900x</Property>
      <Property Name="host.ResponsivenessCheckEnabled" Type="Bool">true</Property>
      <Property Name="host.ResponsivenessCheckPingDelay" Type="UInt">5000</Property>
      <Property Name="host.ResponsivenessCheckPingTimeout" Type="UInt">1000</Property>
      <Property Name="host.TargetCPUID" Type="UInt">3</Property>
      <Property Name="host.TargetOSID" Type="UInt">15</Property>
      <Property Name="target.cleanupVisa" Type="Bool">false</Property>
      <Property Name="target.FPProtocolGlobals_ControlTimeLimit" Type="Int">300</Property>
      <Property Name="target.getDefault-&gt;WebServer.Port" Type="Int">80</Property>
      <Property Name="target.getDefault-&gt;WebServer.Timeout" Type="Int">60</Property>
      <Property Name="target.IsRemotePanelSupported" Type="Bool">true</Property>
      <Property Name="target.RTTarget.ApplicationPath" Type="Path">/c/ni-rt/startup/startup.rtexe</Property>
      <Property Name="target.RTTarget.EnableFileSharing" Type="Bool">true</Property>
      <Property Name="target.RTTarget.IPAccess" Type="Str">+*</Property>
      <Property Name="target.RTTarget.LaunchAppAtBoot" Type="Bool">false</Property>
      <Property Name="target.RTTarget.VIPath" Type="Path">/c/ni-rt/startup</Property>
      <Property Name="target.server.app.propertiesEnabled" Type="Bool">true</Property>
      <Property Name="target.server.control.propertiesEnabled" Type="Bool">true</Property>
      <Property Name="target.server.tcp.access" Type="Str">+*</Property>
      <Property Name="target.server.tcp.enabled" Type="Bool">false</Property>
      <Property Name="target.server.tcp.paranoid" Type="Bool">true</Property>
      <Property Name="target.server.tcp.port" Type="Int">3363</Property>
      <Property Name="target.server.tcp.serviceName" Type="Str">Main Application Instance/VI Server</Property>
      <Property Name="target.server.tcp.serviceName.default" Type="Str">Main Application Instance/VI Server</Property>
      <Property Name="target.server.vi.access" Type="Str">+*</Property>
      <Property Name="target.server.vi.callsEnabled" Type="Bool">true</Property>
      <Property Name="target.server.vi.propertiesEnabled" Type="Bool">true</Property>
      <Property Name="target.WebServer.Enabled" Type="Bool">false</Property>
      <Property Name="target.WebServer.LogEnabled" Type="Bool">false</Property>
      <Property Name="target.WebServer.LogPath" Type="Path">/c/ni-rt/system/www/www.log</Property>
      <Property Name="target.WebServer.Port" Type="Int">80</Property>
      <Property Name="target.WebServer.RootPath" Type="Path">/c/ni-rt/system/www</Property>
      <Property Name="target.WebServer.TcpAccess" Type="Str">c+*</Property>
      <Property Name="target.WebServer.Timeout" Type="Int">60</Property>
      <Property Name="target.WebServer.ViAccess" Type="Str">+*</Property>
      <Item Name="daemon-programs" Type="Folder">
         <Item Name="Control daemon (NTCP).vi" Type="VI" URL="daemon-programs.llb/Control daemon (NTCP).vi"/>
         <Item Name="Server daemon (streaming data).vi" Type="VI" URL="daemon-programs.llb/Server daemon (streaming data).vi"/>
      </Item>
      <Item Name="Subroutines" Type="Folder">
         <Item Name="(Fake) DAQ channels to units.vi" Type="VI" URL="Subroutines.llb/(Fake) DAQ channels to units.vi"/>
         <Item Name="ADXL averaging read.vi" Type="VI" URL="Subroutines.llb/ADXL averaging read.vi"/>
         <Item Name="ADXL open.vi" Type="VI" URL="Subroutines.llb/ADXL open.vi"/>
         <Item Name="ADXL read.vi" Type="VI" URL="Subroutines.llb/ADXL read.vi"/>
         <Item Name="Build FTP full filename.vi" Type="VI" URL="Subroutines.llb/Build FTP full filename.vi"/>
         <Item Name="Command dispatcher.vi" Type="VI" URL="Subroutines.llb/Command dispatcher.vi"/>
         <Item Name="Create new dot-written file.vi" Type="VI" URL="Subroutines.llb/Create new dot-written file.vi"/>
         <Item Name="DAQ channel validator.vi" Type="VI" URL="Subroutines.llb/DAQ channel validator.vi"/>
         <Item Name="DAQ channels to descriptions.vi" Type="VI" URL="Subroutines.llb/DAQ channels to descriptions.vi"/>
         <Item Name="DAQ channels to string array.vi" Type="VI" URL="Subroutines.llb/DAQ channels to string array.vi"/>
         <Item Name="DAQ channels to units.vi" Type="VI" URL="Subroutines.llb/DAQ channels to units.vi"/>
         <Item Name="DAQ status read.vi" Type="VI" URL="Subroutines.llb/DAQ status read.vi"/>
         <Item Name="DAQ status write.vi" Type="VI" URL="Subroutines.llb/DAQ status write.vi"/>
         <Item Name="Data array to datafile stream.vi" Type="VI" URL="Subroutines.llb/Data array to datafile stream.vi"/>
         <Item Name="Data array to RBNB stream.vi" Type="VI" URL="Subroutines.llb/Data array to RBNB stream.vi"/>
         <Item Name="Fake data - gen N pts.vi" Type="VI" URL="../telepresence/neesdaq/Subroutines.llb/Fake data - gen N pts.vi"/>
         <Item Name="Generate timestamp array from waveform array.vi" Type="VI" URL="Subroutines.llb/Generate timestamp array from waveform array.vi"/>
         <Item Name="Generate Waveform.vi" Type="VI" URL="../telepresence/neesdaq/Subroutines.llb/Generate Waveform.vi"/>
         <Item Name="Get status and time.vi" Type="VI" URL="Subroutines.llb/Get status and time.vi"/>
         <Item Name="Global variables.vi" Type="VI" URL="Subroutines.llb/Global variables.vi"/>
         <Item Name="Is channel open.vi" Type="VI" URL="Subroutines.llb/Is channel open.vi"/>
         <Item Name="Launch data streamer.vi" Type="VI" URL="Subroutines.llb/Launch data streamer.vi"/>
         <Item Name="Metadata read.vi" Type="VI" URL="Subroutines.llb/Metadata read.vi"/>
         <Item Name="Metadata save - fake units.vi" Type="VI" URL="Subroutines.llb/Metadata save - fake units.vi"/>
         <Item Name="Metadata save - user-defined units.vi" Type="VI" URL="Subroutines.llb/Metadata save - user-defined units.vi"/>
         <Item Name="Metadata save.vi" Type="VI" URL="Subroutines.llb/Metadata save.vi"/>
         <Item Name="Number of open channels.vi" Type="VI" URL="Subroutines.llb/Number of open channels.vi"/>
         <Item Name="Open or close port.vi" Type="VI" URL="Subroutines.llb/Open or close port.vi"/>
         <Item Name="RBNB stream to data array.vi" Type="VI" URL="Subroutines.llb/RBNB stream to data array.vi"/>
         <Item Name="Real data - gen N pts.vi" Type="VI" URL="Subroutines.llb/Real data - gen N pts.vi"/>
         <Item Name="Save and stream waveform array.vi" Type="VI" URL="Subroutines.llb/Save and stream waveform array.vi"/>
         <Item Name="Select Nth sample from waveform array.vi" Type="VI" URL="Subroutines.llb/Select Nth sample from waveform array.vi"/>
         <Item Name="Serialize data, string channel names.vi" Type="VI" URL="Subroutines.llb/Serialize data, string channel names.vi"/>
         <Item Name="Server daemon stop handler.vi" Type="VI" URL="Subroutines.llb/Server daemon stop handler.vi"/>
         <Item Name="Stream waveform array to RBNB.vi" Type="VI" URL="Subroutines.llb/Stream waveform array to RBNB.vi"/>
         <Item Name="String array to DAQ channels.vi" Type="VI" URL="Subroutines.llb/String array to DAQ channels.vi"/>
         <Item Name="TCP conditional data send.vi" Type="VI" URL="Subroutines.llb/TCP conditional data send.vi"/>
         <Item Name="TCP data enqueue.vi" Type="VI" URL="Subroutines.llb/TCP data enqueue.vi"/>
         <Item Name="TCP read command.vi" Type="VI" URL="Subroutines.llb/TCP read command.vi"/>
         <Item Name="TCP read until EOL.vi" Type="VI" URL="../telepresence/neesdaq/Subroutines.llb/TCP read until EOL.vi"/>
         <Item Name="TCP send with newline.vi" Type="VI" URL="Subroutines.llb/TCP send with newline.vi"/>
         <Item Name="Timestamp.vi" Type="VI" URL="Subroutines.llb/Timestamp.vi"/>
         <Item Name="Waveform array into timestamp and data.vi" Type="VI" URL="Subroutines.llb/Waveform array into timestamp and data.vi"/>
         <Item Name="Write file header, fake units.vi" Type="VI" URL="Subroutines.llb/Write file header, fake units.vi"/>
         <Item Name="Write file header.vi" Type="VI" URL="Subroutines.llb/Write file header.vi"/>
      </Item>
      <Item Name="Time Set" Type="Folder">
         <Item Name="setTime.vi" Type="VI" URL="setTime.vi"/>
         <Item Name="SNTP Get Time.vi" Type="VI" URL="SNTP Get Time.vi"/>
         <Item Name="RT Set Time (farlap).vi" Type="VI" URL="RT Set Time (farlap).vi"/>
         <Item Name="RT Set Time (subsecond).vi" Type="VI" URL="RT Set Time (subsecond).vi"/>
      </Item>
      <Item Name="9205_RT.vi" Type="VI" URL="9205_RT.vi"/>
      <Item Name="9205_baroCal_RT.vi" Type="VI" URL="9205_baroCal_RT.vi"/>
      <Item Name="kxt510_RT.vi" Type="VI" URL="kxt510_RT.vi"/>
      <Item Name="FPGA Target" Type="FPGA Target">
         <Property Name="AutoRun" Type="Bool">true</Property>
         <Property Name="configString.guid" Type="Str">{20F67189-8E58-4428-84A9-56A912B76E78}NI 9205,Slot 2,cRIOModule.AI0.TerminalMode=0,cRIOModule.AI0.VoltageRange=0,cRIOModule.AI1.TerminalMode=0,cRIOModule.AI1.VoltageRange=0,cRIOModule.AI10.TerminalMode=0,cRIOModule.AI10.VoltageRange=0,cRIOModule.AI11.TerminalMode=0,cRIOModule.AI11.VoltageRange=0,cRIOModule.AI12.TerminalMode=0,cRIOModule.AI12.VoltageRange=0,cRIOModule.AI13.TerminalMode=0,cRIOModule.AI13.VoltageRange=0,cRIOModule.AI14.TerminalMode=0,cRIOModule.AI14.VoltageRange=0,cRIOModule.AI15.TerminalMode=0,cRIOModule.AI15.VoltageRange=0,cRIOModule.AI16.TerminalMode=0,cRIOModule.AI16.VoltageRange=0,cRIOModule.AI17.TerminalMode=0,cRIOModule.AI17.VoltageRange=0,cRIOModule.AI18.TerminalMode=0,cRIOModule.AI18.VoltageRange=0,cRIOModule.AI19.TerminalMode=0,cRIOModule.AI19.VoltageRange=0,cRIOModule.AI2.TerminalMode=0,cRIOModule.AI2.VoltageRange=0,cRIOModule.AI20.TerminalMode=0,cRIOModule.AI20.VoltageRange=0,cRIOModule.AI21.TerminalMode=0,cRIOModule.AI21.VoltageRange=0,cRIOModule.AI22.TerminalMode=0,cRIOModule.AI22.VoltageRange=0,cRIOModule.AI23.TerminalMode=0,cRIOModule.AI23.VoltageRange=0,cRIOModule.AI24.TerminalMode=0,cRIOModule.AI24.VoltageRange=0,cRIOModule.AI25.TerminalMode=0,cRIOModule.AI25.VoltageRange=0,cRIOModule.AI26.TerminalMode=0,cRIOModule.AI26.VoltageRange=0,cRIOModule.AI27.TerminalMode=0,cRIOModule.AI27.VoltageRange=0,cRIOModule.AI28.TerminalMode=0,cRIOModule.AI28.VoltageRange=0,cRIOModule.AI29.TerminalMode=0,cRIOModule.AI29.VoltageRange=0,cRIOModule.AI3.TerminalMode=0,cRIOModule.AI3.VoltageRange=0,cRIOModule.AI30.TerminalMode=0,cRIOModule.AI30.VoltageRange=0,cRIOModule.AI31.TerminalMode=0,cRIOModule.AI31.VoltageRange=0,cRIOModule.AI4.TerminalMode=0,cRIOModule.AI4.VoltageRange=0,cRIOModule.AI5.TerminalMode=0,cRIOModule.AI5.VoltageRange=0,cRIOModule.AI6.TerminalMode=0,cRIOModule.AI6.VoltageRange=0,cRIOModule.AI7.TerminalMode=0,cRIOModule.AI7.VoltageRange=0,cRIOModule.AI8.TerminalMode=0,cRIOModule.AI8.VoltageRange=0,cRIOModule.AI9.TerminalMode=0,cRIOModule.AI9.VoltageRange=0,cRIOModule.EnableCalProperties=false,cRIOModule.MinConvTime=8.000000{2739E25F-5E41-45D1-BA32-ACA65BF1ED63}resource=/Chassis Temperature;0;ReadMethodType=i16{398E439F-2F82-4D4D-BFC5-4849016FE4DD}"Depth=4095;Width=4;Dir=0;Strategy=1;Read Arbs=Optimize For Single;Write Arbs=Optimize For Single;Type=2;Channel=2;Write=1ctempDmaFIFO"{6E19D78A-3707-43C8-9372-4E60BACCD69F}###!!A!!!")!&amp;E!Q`````QV3:8.P&gt;8*D:3"/97VF!"R!-0````]36'^Q)&amp;.J:WZB&lt;#"$&lt;WZO:7.U!!!;1$$`````%5.M&lt;W.L)&amp;.J:WZB&lt;#"/97VF!"B!#B*.;7YA2H*F=86F&lt;G.Z)#B)?CE!!"B!#B*.98AA2H*F=86F&lt;G.Z)#B)?CE!!"B!)2*798*J97*M:3"'=G6R&gt;76O9XE!!"R!#B:/&lt;WVJ&lt;G&amp;M)%:S:8&amp;V:7ZD?3!I3(IJ!!!=1!I85'6B;S"1:8*J&lt;W1A3GFU&gt;'6S)#BQ=SE!(%!+&amp;UVJ&lt;C"%&gt;82Z)%.Z9WRF)#AF)%BJ:WAJ!"R!#B&gt;.98AA2(6U?3"$?7.M:3!I*3");7&gt;I+1!51!I/17.D&gt;8*B9XEA+("Q&lt;3E!!"*!)1R'=G6F)&amp;*V&lt;GZJ&lt;G=!!"2!)1^4=(*F971A5X"F9X2S&gt;7U!%E!Q`````QB$&lt;'^D;S"*2!!!/%"!!!(`````!!UK5G6M982F:#"$&lt;'^D;X-A&gt;WFU;#"O&lt;S"$2%-A9W^N=(-A&lt;G6D:8.T98*Z!!!31&amp;--2W6O:8*J9S"%982B!!!/1$$`````"5&amp;M;7&amp;T!']!]1!!!!!!!!!")'ZJ=H:J1G&amp;T:624382F&lt;5.P&lt;G:J:X6S982J&lt;WYO9X2M!%6!5!!1!!!!!1!#!!-!"!!&amp;!!9!"Q!)!!E!#A!,!!Q!$A!0!"!&lt;1X6S=G6O&gt;#"$&lt;'^D;S"$&lt;WZG;7&gt;V=G&amp;U;7^O!!%!%1!!!"1U-#".3(IA4WZC&lt;W&amp;S:#"$&lt;'^D;Q!!!!6$&lt;'MU-!!!!!6$&lt;'MU-%'$%N!!!!!!19-3U!!!!!!!19-3U!!!!!"!&lt;U!!!!!!!%"*!!!!!!!!1%E!!!!!!!"!71!!!!!!!!%!!!!!!!AAA!)!!!!"!!1!!!!"!!!!!!!!!!!!&amp;$1Q)%V)?C"0&lt;G*P98*E)%.M&lt;W.L!!!!!!{7DE9DEAA-991D-4D97-83D7-2AA568C60332}"Depth=4095;Width=4;Dir=0;Strategy=1;Read Arbs=Optimize For Single;Write Arbs=Optimize For Single;Type=2;Channel=1;Write=1AI1DmaFIFO"{89BA6905-BB05-426E-BB32-86EC05DED589}resource=/crio_NI 9205/AI0;0;ReadMethodType=i16{8F30ECE1-79D0-4333-A50E-2C502B6DDC23}resource=/crio_NI 9205/AI3;0;ReadMethodType=i16{8F567677-804D-4DD5-BF28-47F23E53C5BA}"Depth=4095;Width=4;Dir=0;Strategy=1;Read Arbs=Optimize For Single;Write Arbs=Optimize For Single;Type=2;Channel=0;Write=1AI0DmaFIFO"{9F76D822-361E-431A-A82B-AB8480220F7C}resource=/crio_NI 9205/AI2;0;ReadMethodType=i16{AE532BEC-EAA7-4A9B-B7FF-1D226A927D4B}resource=/crio_NI 9205/DI0;0;ReadMethodType=bool{C61FABAF-58AB-444E-998B-DF9B7E8961AF}resource=/crio_NI 9205/DO0;0;WriteMethodType=bool{FD191A67-5F7B-4457-9C5F-C23774C338F9}resource=/crio_NI 9205/AI1;0;ReadMethodType=i16cRIO-9104/Clk40/40 MHz Onboard ClocktrueTARGET_TYPEFPGA</Property>
         <Property Name="configString.name" Type="Str">40 MHz Onboard Clock###!!A!!!")!&amp;E!Q`````QV3:8.P&gt;8*D:3"/97VF!"R!-0````]36'^Q)&amp;.J:WZB&lt;#"$&lt;WZO:7.U!!!;1$$`````%5.M&lt;W.L)&amp;.J:WZB&lt;#"/97VF!"B!#B*.;7YA2H*F=86F&lt;G.Z)#B)?CE!!"B!#B*.98AA2H*F=86F&lt;G.Z)#B)?CE!!"B!)2*798*J97*M:3"'=G6R&gt;76O9XE!!"R!#B:/&lt;WVJ&lt;G&amp;M)%:S:8&amp;V:7ZD?3!I3(IJ!!!=1!I85'6B;S"1:8*J&lt;W1A3GFU&gt;'6S)#BQ=SE!(%!+&amp;UVJ&lt;C"%&gt;82Z)%.Z9WRF)#AF)%BJ:WAJ!"R!#B&gt;.98AA2(6U?3"$?7.M:3!I*3");7&gt;I+1!51!I/17.D&gt;8*B9XEA+("Q&lt;3E!!"*!)1R'=G6F)&amp;*V&lt;GZJ&lt;G=!!"2!)1^4=(*F971A5X"F9X2S&gt;7U!%E!Q`````QB$&lt;'^D;S"*2!!!/%"!!!(`````!!UK5G6M982F:#"$&lt;'^D;X-A&gt;WFU;#"O&lt;S"$2%-A9W^N=(-A&lt;G6D:8.T98*Z!!!31&amp;--2W6O:8*J9S"%982B!!!/1$$`````"5&amp;M;7&amp;T!']!]1!!!!!!!!!")'ZJ=H:J1G&amp;T:624382F&lt;5.P&lt;G:J:X6S982J&lt;WYO9X2M!%6!5!!1!!!!!1!#!!-!"!!&amp;!!9!"Q!)!!E!#A!,!!Q!$A!0!"!&lt;1X6S=G6O&gt;#"$&lt;'^D;S"$&lt;WZG;7&gt;V=G&amp;U;7^O!!%!%1!!!"1U-#".3(IA4WZC&lt;W&amp;S:#"$&lt;'^D;Q!!!!6$&lt;'MU-!!!!!6$&lt;'MU-%'$%N!!!!!!19-3U!!!!!!!19-3U!!!!!"!&lt;U!!!!!!!%"*!!!!!!!!1%E!!!!!!!"!71!!!!!!!!%!!!!!!!AAA!)!!!!"!!1!!!!"!!!!!!!!!!!!&amp;$1Q)%V)?C"0&lt;G*P98*E)%.M&lt;W.L!!!!!!AI0DmaFIFO"Depth=4095;Width=4;Dir=0;Strategy=1;Read Arbs=Optimize For Single;Write Arbs=Optimize For Single;Type=2;Channel=0;Write=1AI0DmaFIFO"AI0resource=/crio_NI 9205/AI0;0;ReadMethodType=i16AI1DmaFIFO"Depth=4095;Width=4;Dir=0;Strategy=1;Read Arbs=Optimize For Single;Write Arbs=Optimize For Single;Type=2;Channel=1;Write=1AI1DmaFIFO"AI1resource=/crio_NI 9205/AI1;0;ReadMethodType=i16AI2resource=/crio_NI 9205/AI2;0;ReadMethodType=i16AI3resource=/crio_NI 9205/AI3;0;ReadMethodType=i16Chassis Temperatureresource=/Chassis Temperature;0;ReadMethodType=i16cRIO-9104/Clk40/40 MHz Onboard ClocktrueTARGET_TYPEFPGActempDmaFIFO"Depth=4095;Width=4;Dir=0;Strategy=1;Read Arbs=Optimize For Single;Write Arbs=Optimize For Single;Type=2;Channel=2;Write=1ctempDmaFIFO"DI0resource=/crio_NI 9205/DI0;0;ReadMethodType=boolDO0resource=/crio_NI 9205/DO0;0;WriteMethodType=boolNI 9205NI 9205,Slot 2,cRIOModule.AI0.TerminalMode=0,cRIOModule.AI0.VoltageRange=0,cRIOModule.AI1.TerminalMode=0,cRIOModule.AI1.VoltageRange=0,cRIOModule.AI10.TerminalMode=0,cRIOModule.AI10.VoltageRange=0,cRIOModule.AI11.TerminalMode=0,cRIOModule.AI11.VoltageRange=0,cRIOModule.AI12.TerminalMode=0,cRIOModule.AI12.VoltageRange=0,cRIOModule.AI13.TerminalMode=0,cRIOModule.AI13.VoltageRange=0,cRIOModule.AI14.TerminalMode=0,cRIOModule.AI14.VoltageRange=0,cRIOModule.AI15.TerminalMode=0,cRIOModule.AI15.VoltageRange=0,cRIOModule.AI16.TerminalMode=0,cRIOModule.AI16.VoltageRange=0,cRIOModule.AI17.TerminalMode=0,cRIOModule.AI17.VoltageRange=0,cRIOModule.AI18.TerminalMode=0,cRIOModule.AI18.VoltageRange=0,cRIOModule.AI19.TerminalMode=0,cRIOModule.AI19.VoltageRange=0,cRIOModule.AI2.TerminalMode=0,cRIOModule.AI2.VoltageRange=0,cRIOModule.AI20.TerminalMode=0,cRIOModule.AI20.VoltageRange=0,cRIOModule.AI21.TerminalMode=0,cRIOModule.AI21.VoltageRange=0,cRIOModule.AI22.TerminalMode=0,cRIOModule.AI22.VoltageRange=0,cRIOModule.AI23.TerminalMode=0,cRIOModule.AI23.VoltageRange=0,cRIOModule.AI24.TerminalMode=0,cRIOModule.AI24.VoltageRange=0,cRIOModule.AI25.TerminalMode=0,cRIOModule.AI25.VoltageRange=0,cRIOModule.AI26.TerminalMode=0,cRIOModule.AI26.VoltageRange=0,cRIOModule.AI27.TerminalMode=0,cRIOModule.AI27.VoltageRange=0,cRIOModule.AI28.TerminalMode=0,cRIOModule.AI28.VoltageRange=0,cRIOModule.AI29.TerminalMode=0,cRIOModule.AI29.VoltageRange=0,cRIOModule.AI3.TerminalMode=0,cRIOModule.AI3.VoltageRange=0,cRIOModule.AI30.TerminalMode=0,cRIOModule.AI30.VoltageRange=0,cRIOModule.AI31.TerminalMode=0,cRIOModule.AI31.VoltageRange=0,cRIOModule.AI4.TerminalMode=0,cRIOModule.AI4.VoltageRange=0,cRIOModule.AI5.TerminalMode=0,cRIOModule.AI5.VoltageRange=0,cRIOModule.AI6.TerminalMode=0,cRIOModule.AI6.VoltageRange=0,cRIOModule.AI7.TerminalMode=0,cRIOModule.AI7.VoltageRange=0,cRIOModule.AI8.TerminalMode=0,cRIOModule.AI8.VoltageRange=0,cRIOModule.AI9.TerminalMode=0,cRIOModule.AI9.VoltageRange=0,cRIOModule.EnableCalProperties=false,cRIOModule.MinConvTime=8.000000</Property>
         <Property Name="Mode" Type="Int">0</Property>
         <Property Name="NI.LV.FPGA.CompileConfigString" Type="Str">cRIO-9104/Clk40/40 MHz Onboard ClocktrueTARGET_TYPEFPGA</Property>
         <Property Name="NI.LV.FPGA.Version" Type="Int">3</Property>
         <Property Name="Resource Name" Type="Str">RIO0::INSTR</Property>
         <Property Name="Target Class" Type="Str">cRIO-9104</Property>
         <Property Name="Top-Level Timing Source" Type="Str">40 MHz Onboard Clock</Property>
         <Property Name="Top-Level Timing Source Is Default" Type="Bool">true</Property>
         <Item Name="Analog Input" Type="Folder">
            <Item Name="NI 9205" Type="Folder">
               <Item Name="AI0" Type="Elemental IO">
                  <Property Name="eioAttrBag" Type="Xml"><AttributeSet name="FPGA Target">
   <Attribute name="resource">
   <Value>/crio_NI 9205/AI0</Value>
   </Attribute>
</AttributeSet>
</Property>
                  <Property Name="FPGA.PersistentID" Type="Str">{89BA6905-BB05-426E-BB32-86EC05DED589}</Property>
               </Item>
               <Item Name="AI1" Type="Elemental IO">
                  <Property Name="eioAttrBag" Type="Xml"><AttributeSet name="FPGA Target">
   <Attribute name="resource">
   <Value>/crio_NI 9205/AI1</Value>
   </Attribute>
</AttributeSet>
</Property>
                  <Property Name="FPGA.PersistentID" Type="Str">{FD191A67-5F7B-4457-9C5F-C23774C338F9}</Property>
               </Item>
               <Item Name="AI2" Type="Elemental IO">
                  <Property Name="eioAttrBag" Type="Xml"><AttributeSet name="FPGA Target">
   <Attribute name="resource">
   <Value>/crio_NI 9205/AI2</Value>
   </Attribute>
</AttributeSet>
</Property>
                  <Property Name="FPGA.PersistentID" Type="Str">{9F76D822-361E-431A-A82B-AB8480220F7C}</Property>
               </Item>
               <Item Name="AI3" Type="Elemental IO">
                  <Property Name="eioAttrBag" Type="Xml"><AttributeSet name="FPGA Target">
   <Attribute name="resource">
   <Value>/crio_NI 9205/AI3</Value>
   </Attribute>
</AttributeSet>
</Property>
                  <Property Name="FPGA.PersistentID" Type="Str">{8F30ECE1-79D0-4333-A50E-2C502B6DDC23}</Property>
               </Item>
            </Item>
            <Item Name="Chassis Temperature" Type="Elemental IO">
               <Property Name="eioAttrBag" Type="Xml"><AttributeSet name="FPGA Target">
   <Attribute name="resource">
   <Value>/Chassis Temperature</Value>
   </Attribute>
</AttributeSet>
</Property>
               <Property Name="FPGA.PersistentID" Type="Str">{2739E25F-5E41-45D1-BA32-ACA65BF1ED63}</Property>
            </Item>
         </Item>
         <Item Name="Digital Line Input" Type="Folder">
            <Item Name="NI 9205" Type="Folder">
               <Item Name="DI0" Type="Elemental IO">
                  <Property Name="eioAttrBag" Type="Xml"><AttributeSet name="FPGA Target">
   <Attribute name="resource">
   <Value>/crio_NI 9205/DI0</Value>
   </Attribute>
</AttributeSet>
</Property>
                  <Property Name="FPGA.PersistentID" Type="Str">{AE532BEC-EAA7-4A9B-B7FF-1D226A927D4B}</Property>
               </Item>
            </Item>
         </Item>
         <Item Name="Digital Line Output" Type="Folder">
            <Item Name="NI 9205" Type="Folder">
               <Item Name="DO0" Type="Elemental IO">
                  <Property Name="eioAttrBag" Type="Xml"><AttributeSet name="FPGA Target">
   <Attribute name="resource">
   <Value>/crio_NI 9205/DO0</Value>
   </Attribute>
</AttributeSet>
</Property>
                  <Property Name="FPGA.PersistentID" Type="Str">{C61FABAF-58AB-444E-998B-DF9B7E8961AF}</Property>
               </Item>
            </Item>
         </Item>
         <Item Name="9205_FPGA.vi" Type="VI" URL="9205_FPGA.vi">
            <Property Name="configString.guid" Type="Str">{20F67189-8E58-4428-84A9-56A912B76E78}NI 9205,Slot 2,cRIOModule.AI0.TerminalMode=0,cRIOModule.AI0.VoltageRange=0,cRIOModule.AI1.TerminalMode=0,cRIOModule.AI1.VoltageRange=0,cRIOModule.AI10.TerminalMode=0,cRIOModule.AI10.VoltageRange=0,cRIOModule.AI11.TerminalMode=0,cRIOModule.AI11.VoltageRange=0,cRIOModule.AI12.TerminalMode=0,cRIOModule.AI12.VoltageRange=0,cRIOModule.AI13.TerminalMode=0,cRIOModule.AI13.VoltageRange=0,cRIOModule.AI14.TerminalMode=0,cRIOModule.AI14.VoltageRange=0,cRIOModule.AI15.TerminalMode=0,cRIOModule.AI15.VoltageRange=0,cRIOModule.AI16.TerminalMode=0,cRIOModule.AI16.VoltageRange=0,cRIOModule.AI17.TerminalMode=0,cRIOModule.AI17.VoltageRange=0,cRIOModule.AI18.TerminalMode=0,cRIOModule.AI18.VoltageRange=0,cRIOModule.AI19.TerminalMode=0,cRIOModule.AI19.VoltageRange=0,cRIOModule.AI2.TerminalMode=0,cRIOModule.AI2.VoltageRange=0,cRIOModule.AI20.TerminalMode=0,cRIOModule.AI20.VoltageRange=0,cRIOModule.AI21.TerminalMode=0,cRIOModule.AI21.VoltageRange=0,cRIOModule.AI22.TerminalMode=0,cRIOModule.AI22.VoltageRange=0,cRIOModule.AI23.TerminalMode=0,cRIOModule.AI23.VoltageRange=0,cRIOModule.AI24.TerminalMode=0,cRIOModule.AI24.VoltageRange=0,cRIOModule.AI25.TerminalMode=0,cRIOModule.AI25.VoltageRange=0,cRIOModule.AI26.TerminalMode=0,cRIOModule.AI26.VoltageRange=0,cRIOModule.AI27.TerminalMode=0,cRIOModule.AI27.VoltageRange=0,cRIOModule.AI28.TerminalMode=0,cRIOModule.AI28.VoltageRange=0,cRIOModule.AI29.TerminalMode=0,cRIOModule.AI29.VoltageRange=0,cRIOModule.AI3.TerminalMode=0,cRIOModule.AI3.VoltageRange=0,cRIOModule.AI30.TerminalMode=0,cRIOModule.AI30.VoltageRange=0,cRIOModule.AI31.TerminalMode=0,cRIOModule.AI31.VoltageRange=0,cRIOModule.AI4.TerminalMode=0,cRIOModule.AI4.VoltageRange=0,cRIOModule.AI5.TerminalMode=0,cRIOModule.AI5.VoltageRange=0,cRIOModule.AI6.TerminalMode=0,cRIOModule.AI6.VoltageRange=0,cRIOModule.AI7.TerminalMode=0,cRIOModule.AI7.VoltageRange=0,cRIOModule.AI8.TerminalMode=0,cRIOModule.AI8.VoltageRange=0,cRIOModule.AI9.TerminalMode=0,cRIOModule.AI9.VoltageRange=0,cRIOModule.EnableCalProperties=false,cRIOModule.MinConvTime=8.000000{2739E25F-5E41-45D1-BA32-ACA65BF1ED63}resource=/Chassis Temperature;0;ReadMethodType=i16{398E439F-2F82-4D4D-BFC5-4849016FE4DD}"Depth=4095;Width=4;Dir=0;Strategy=1;Read Arbs=Optimize For Single;Write Arbs=Optimize For Single;Type=2;Channel=2;Write=1ctempDmaFIFO"{6E19D78A-3707-43C8-9372-4E60BACCD69F}###!!A!!!")!&amp;E!Q`````QV3:8.P&gt;8*D:3"/97VF!"R!-0````]36'^Q)&amp;.J:WZB&lt;#"$&lt;WZO:7.U!!!;1$$`````%5.M&lt;W.L)&amp;.J:WZB&lt;#"/97VF!"B!#B*.;7YA2H*F=86F&lt;G.Z)#B)?CE!!"B!#B*.98AA2H*F=86F&lt;G.Z)#B)?CE!!"B!)2*798*J97*M:3"'=G6R&gt;76O9XE!!"R!#B:/&lt;WVJ&lt;G&amp;M)%:S:8&amp;V:7ZD?3!I3(IJ!!!=1!I85'6B;S"1:8*J&lt;W1A3GFU&gt;'6S)#BQ=SE!(%!+&amp;UVJ&lt;C"%&gt;82Z)%.Z9WRF)#AF)%BJ:WAJ!"R!#B&gt;.98AA2(6U?3"$?7.M:3!I*3");7&gt;I+1!51!I/17.D&gt;8*B9XEA+("Q&lt;3E!!"*!)1R'=G6F)&amp;*V&lt;GZJ&lt;G=!!"2!)1^4=(*F971A5X"F9X2S&gt;7U!%E!Q`````QB$&lt;'^D;S"*2!!!/%"!!!(`````!!UK5G6M982F:#"$&lt;'^D;X-A&gt;WFU;#"O&lt;S"$2%-A9W^N=(-A&lt;G6D:8.T98*Z!!!31&amp;--2W6O:8*J9S"%982B!!!/1$$`````"5&amp;M;7&amp;T!']!]1!!!!!!!!!")'ZJ=H:J1G&amp;T:624382F&lt;5.P&lt;G:J:X6S982J&lt;WYO9X2M!%6!5!!1!!!!!1!#!!-!"!!&amp;!!9!"Q!)!!E!#A!,!!Q!$A!0!"!&lt;1X6S=G6O&gt;#"$&lt;'^D;S"$&lt;WZG;7&gt;V=G&amp;U;7^O!!%!%1!!!"1U-#".3(IA4WZC&lt;W&amp;S:#"$&lt;'^D;Q!!!!6$&lt;'MU-!!!!!6$&lt;'MU-%'$%N!!!!!!19-3U!!!!!!!19-3U!!!!!"!&lt;U!!!!!!!%"*!!!!!!!!1%E!!!!!!!"!71!!!!!!!!%!!!!!!!AAA!)!!!!"!!1!!!!"!!!!!!!!!!!!&amp;$1Q)%V)?C"0&lt;G*P98*E)%.M&lt;W.L!!!!!!{7DE9DEAA-991D-4D97-83D7-2AA568C60332}"Depth=4095;Width=4;Dir=0;Strategy=1;Read Arbs=Optimize For Single;Write Arbs=Optimize For Single;Type=2;Channel=1;Write=1AI1DmaFIFO"{89BA6905-BB05-426E-BB32-86EC05DED589}resource=/crio_NI 9205/AI0;0;ReadMethodType=i16{8F30ECE1-79D0-4333-A50E-2C502B6DDC23}resource=/crio_NI 9205/AI3;0;ReadMethodType=i16{8F567677-804D-4DD5-BF28-47F23E53C5BA}"Depth=4095;Width=4;Dir=0;Strategy=1;Read Arbs=Optimize For Single;Write Arbs=Optimize For Single;Type=2;Channel=0;Write=1AI0DmaFIFO"{9F76D822-361E-431A-A82B-AB8480220F7C}resource=/crio_NI 9205/AI2;0;ReadMethodType=i16{AE532BEC-EAA7-4A9B-B7FF-1D226A927D4B}resource=/crio_NI 9205/DI0;0;ReadMethodType=bool{C61FABAF-58AB-444E-998B-DF9B7E8961AF}resource=/crio_NI 9205/DO0;0;WriteMethodType=bool{FD191A67-5F7B-4457-9C5F-C23774C338F9}resource=/crio_NI 9205/AI1;0;ReadMethodType=i16cRIO-9104/Clk40/40 MHz Onboard ClocktrueTARGET_TYPEFPGA</Property>
            <Property Name="configString.name" Type="Str">40 MHz Onboard Clock###!!A!!!")!&amp;E!Q`````QV3:8.P&gt;8*D:3"/97VF!"R!-0````]36'^Q)&amp;.J:WZB&lt;#"$&lt;WZO:7.U!!!;1$$`````%5.M&lt;W.L)&amp;.J:WZB&lt;#"/97VF!"B!#B*.;7YA2H*F=86F&lt;G.Z)#B)?CE!!"B!#B*.98AA2H*F=86F&lt;G.Z)#B)?CE!!"B!)2*798*J97*M:3"'=G6R&gt;76O9XE!!"R!#B:/&lt;WVJ&lt;G&amp;M)%:S:8&amp;V:7ZD?3!I3(IJ!!!=1!I85'6B;S"1:8*J&lt;W1A3GFU&gt;'6S)#BQ=SE!(%!+&amp;UVJ&lt;C"%&gt;82Z)%.Z9WRF)#AF)%BJ:WAJ!"R!#B&gt;.98AA2(6U?3"$?7.M:3!I*3");7&gt;I+1!51!I/17.D&gt;8*B9XEA+("Q&lt;3E!!"*!)1R'=G6F)&amp;*V&lt;GZJ&lt;G=!!"2!)1^4=(*F971A5X"F9X2S&gt;7U!%E!Q`````QB$&lt;'^D;S"*2!!!/%"!!!(`````!!UK5G6M982F:#"$&lt;'^D;X-A&gt;WFU;#"O&lt;S"$2%-A9W^N=(-A&lt;G6D:8.T98*Z!!!31&amp;--2W6O:8*J9S"%982B!!!/1$$`````"5&amp;M;7&amp;T!']!]1!!!!!!!!!")'ZJ=H:J1G&amp;T:624382F&lt;5.P&lt;G:J:X6S982J&lt;WYO9X2M!%6!5!!1!!!!!1!#!!-!"!!&amp;!!9!"Q!)!!E!#A!,!!Q!$A!0!"!&lt;1X6S=G6O&gt;#"$&lt;'^D;S"$&lt;WZG;7&gt;V=G&amp;U;7^O!!%!%1!!!"1U-#".3(IA4WZC&lt;W&amp;S:#"$&lt;'^D;Q!!!!6$&lt;'MU-!!!!!6$&lt;'MU-%'$%N!!!!!!19-3U!!!!!!!19-3U!!!!!"!&lt;U!!!!!!!%"*!!!!!!!!1%E!!!!!!!"!71!!!!!!!!%!!!!!!!AAA!)!!!!"!!1!!!!"!!!!!!!!!!!!&amp;$1Q)%V)?C"0&lt;G*P98*E)%.M&lt;W.L!!!!!!AI0DmaFIFO"Depth=4095;Width=4;Dir=0;Strategy=1;Read Arbs=Optimize For Single;Write Arbs=Optimize For Single;Type=2;Channel=0;Write=1AI0DmaFIFO"AI0resource=/crio_NI 9205/AI0;0;ReadMethodType=i16AI1DmaFIFO"Depth=4095;Width=4;Dir=0;Strategy=1;Read Arbs=Optimize For Single;Write Arbs=Optimize For Single;Type=2;Channel=1;Write=1AI1DmaFIFO"AI1resource=/crio_NI 9205/AI1;0;ReadMethodType=i16AI2resource=/crio_NI 9205/AI2;0;ReadMethodType=i16AI3resource=/crio_NI 9205/AI3;0;ReadMethodType=i16Chassis Temperatureresource=/Chassis Temperature;0;ReadMethodType=i16cRIO-9104/Clk40/40 MHz Onboard ClocktrueTARGET_TYPEFPGActempDmaFIFO"Depth=4095;Width=4;Dir=0;Strategy=1;Read Arbs=Optimize For Single;Write Arbs=Optimize For Single;Type=2;Channel=2;Write=1ctempDmaFIFO"DI0resource=/crio_NI 9205/DI0;0;ReadMethodType=boolDO0resource=/crio_NI 9205/DO0;0;WriteMethodType=boolNI 9205NI 9205,Slot 2,cRIOModule.AI0.TerminalMode=0,cRIOModule.AI0.VoltageRange=0,cRIOModule.AI1.TerminalMode=0,cRIOModule.AI1.VoltageRange=0,cRIOModule.AI10.TerminalMode=0,cRIOModule.AI10.VoltageRange=0,cRIOModule.AI11.TerminalMode=0,cRIOModule.AI11.VoltageRange=0,cRIOModule.AI12.TerminalMode=0,cRIOModule.AI12.VoltageRange=0,cRIOModule.AI13.TerminalMode=0,cRIOModule.AI13.VoltageRange=0,cRIOModule.AI14.TerminalMode=0,cRIOModule.AI14.VoltageRange=0,cRIOModule.AI15.TerminalMode=0,cRIOModule.AI15.VoltageRange=0,cRIOModule.AI16.TerminalMode=0,cRIOModule.AI16.VoltageRange=0,cRIOModule.AI17.TerminalMode=0,cRIOModule.AI17.VoltageRange=0,cRIOModule.AI18.TerminalMode=0,cRIOModule.AI18.VoltageRange=0,cRIOModule.AI19.TerminalMode=0,cRIOModule.AI19.VoltageRange=0,cRIOModule.AI2.TerminalMode=0,cRIOModule.AI2.VoltageRange=0,cRIOModule.AI20.TerminalMode=0,cRIOModule.AI20.VoltageRange=0,cRIOModule.AI21.TerminalMode=0,cRIOModule.AI21.VoltageRange=0,cRIOModule.AI22.TerminalMode=0,cRIOModule.AI22.VoltageRange=0,cRIOModule.AI23.TerminalMode=0,cRIOModule.AI23.VoltageRange=0,cRIOModule.AI24.TerminalMode=0,cRIOModule.AI24.VoltageRange=0,cRIOModule.AI25.TerminalMode=0,cRIOModule.AI25.VoltageRange=0,cRIOModule.AI26.TerminalMode=0,cRIOModule.AI26.VoltageRange=0,cRIOModule.AI27.TerminalMode=0,cRIOModule.AI27.VoltageRange=0,cRIOModule.AI28.TerminalMode=0,cRIOModule.AI28.VoltageRange=0,cRIOModule.AI29.TerminalMode=0,cRIOModule.AI29.VoltageRange=0,cRIOModule.AI3.TerminalMode=0,cRIOModule.AI3.VoltageRange=0,cRIOModule.AI30.TerminalMode=0,cRIOModule.AI30.VoltageRange=0,cRIOModule.AI31.TerminalMode=0,cRIOModule.AI31.VoltageRange=0,cRIOModule.AI4.TerminalMode=0,cRIOModule.AI4.VoltageRange=0,cRIOModule.AI5.TerminalMode=0,cRIOModule.AI5.VoltageRange=0,cRIOModule.AI6.TerminalMode=0,cRIOModule.AI6.VoltageRange=0,cRIOModule.AI7.TerminalMode=0,cRIOModule.AI7.VoltageRange=0,cRIOModule.AI8.TerminalMode=0,cRIOModule.AI8.VoltageRange=0,cRIOModule.AI9.TerminalMode=0,cRIOModule.AI9.VoltageRange=0,cRIOModule.EnableCalProperties=false,cRIOModule.MinConvTime=8.000000</Property>
            <Property Name="NI.LV.FPGA.InterfaceBitfile" Type="Str">C:\Documents and Settings\ljmiller\Desktop\svntmp\FPGA Bitfiles\cleosRio.lvproj_FPGA Target_9205_FPGA.vi.lvbit</Property>
         </Item>
         <Item Name="40 MHz Onboard Clock" Type="FPGA Base Clock">
            <Property Name="FPGA.PersistentID" Type="Str">{6E19D78A-3707-43C8-9372-4E60BACCD69F}</Property>
            <Property Name="NI.LV.FPGA.BaseTSConfig" Type="Bin">###!!A!!!")!&amp;E!Q`````QV3:8.P&gt;8*D:3"/97VF!"R!-0````]36'^Q)&amp;.J:WZB&lt;#"$&lt;WZO:7.U!!!;1$$`````%5.M&lt;W.L)&amp;.J:WZB&lt;#"/97VF!"B!#B*.;7YA2H*F=86F&lt;G.Z)#B)?CE!!"B!#B*.98AA2H*F=86F&lt;G.Z)#B)?CE!!"B!)2*798*J97*M:3"'=G6R&gt;76O9XE!!"R!#B:/&lt;WVJ&lt;G&amp;M)%:S:8&amp;V:7ZD?3!I3(IJ!!!=1!I85'6B;S"1:8*J&lt;W1A3GFU&gt;'6S)#BQ=SE!(%!+&amp;UVJ&lt;C"%&gt;82Z)%.Z9WRF)#AF)%BJ:WAJ!"R!#B&gt;.98AA2(6U?3"$?7.M:3!I*3");7&gt;I+1!51!I/17.D&gt;8*B9XEA+("Q&lt;3E!!"*!)1R'=G6F)&amp;*V&lt;GZJ&lt;G=!!"2!)1^4=(*F971A5X"F9X2S&gt;7U!%E!Q`````QB$&lt;'^D;S"*2!!!/%"!!!(`````!!UK5G6M982F:#"$&lt;'^D;X-A&gt;WFU;#"O&lt;S"$2%-A9W^N=(-A&lt;G6D:8.T98*Z!!!31&amp;--2W6O:8*J9S"%982B!!!/1$$`````"5&amp;M;7&amp;T!']!]1!!!!!!!!!")'ZJ=H:J1G&amp;T:624382F&lt;5.P&lt;G:J:X6S982J&lt;WYO9X2M!%6!5!!1!!!!!1!#!!-!"!!&amp;!!9!"Q!)!!E!#A!,!!Q!$A!0!"!&lt;1X6S=G6O&gt;#"$&lt;'^D;S"$&lt;WZG;7&gt;V=G&amp;U;7^O!!%!%1!!!"1U-#".3(IA4WZC&lt;W&amp;S:#"$&lt;'^D;Q!!!!6$&lt;'MU-!!!!!6$&lt;'MU-%'$%N!!!!!!19-3U!!!!!!!19-3U!!!!!"!&lt;U!!!!!!!%"*!!!!!!!!1%E!!!!!!!"!71!!!!!!!!%!!!!!!!AAA!)!!!!"!!1!!!!"!!!!!!!!!!!!&amp;$1Q)%V)?C"0&lt;G*P98*E)%.M&lt;W.L!!!!!!</Property>
            <Property Name="NI.LV.FPGA.Valid" Type="Bool">true</Property>
            <Property Name="NI.LV.FPGA.Version" Type="Int">2</Property>
         </Item>
         <Item Name="NI 9205" Type="RIO C Series Module">
            <Property Name="crio.Location" Type="Str">Slot 2</Property>
            <Property Name="crio.RequiresValidation" Type="Bool">false</Property>
            <Property Name="crio.SupportsDynamicRes" Type="Bool">false</Property>
            <Property Name="crio.Type" Type="Str">NI 9205</Property>
            <Property Name="cRIOModule.AI0.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI0.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI1.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI1.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI10.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI10.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI11.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI11.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI12.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI12.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI13.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI13.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI14.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI14.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI15.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI15.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI16.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI16.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI17.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI17.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI18.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI18.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI19.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI19.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI2.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI2.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI20.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI20.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI21.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI21.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI22.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI22.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI23.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI23.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI24.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI24.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI25.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI25.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI26.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI26.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI27.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI27.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI28.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI28.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI29.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI29.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI3.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI3.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI30.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI30.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI31.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI31.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI4.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI4.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI5.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI5.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI6.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI6.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI7.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI7.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI8.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI8.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.AI9.TerminalMode" Type="Str">0</Property>
            <Property Name="cRIOModule.AI9.VoltageRange" Type="Str">0</Property>
            <Property Name="cRIOModule.EnableCalProperties" Type="Str">false</Property>
            <Property Name="cRIOModule.MinConvTime" Type="Str">8.000000</Property>
            <Property Name="FPGA.PersistentID" Type="Str">{20F67189-8E58-4428-84A9-56A912B76E78}</Property>
         </Item>
         <Item Name="AI0DmaFIFO" Type="FPGA FIFO">
            <Property Name="fifo.configuration" Type="Str">"Depth=4095;Width=4;Dir=0;Strategy=1;Read Arbs=Optimize For Single;Write Arbs=Optimize For Single;Type=2;Channel=0;Write=1AI0DmaFIFO"</Property>
            <Property Name="fifo.configured" Type="Bool">true</Property>
            <Property Name="fifo.state" Type="Bin">###!!A!!!!U!$E!Q`````Q2/97VF!!!+1!=&amp;2'6Q&gt;'A!.1$R!!!!!!!!!!%92GFG&lt;V^%982B6(FQ:5.P&lt;H2S&lt;WQO9X2M!".!!AJ%982B)&amp;&gt;J:(2I!!!R!0%!!!!!!!!!!2*';7:P8U2J=G6D&gt;'FP&lt;CZD&gt;'Q!&amp;5!$$5:*2E^%;8*F9X2J&lt;WY!-!$R!!!!!!!!!!%42GFG&lt;V^*4V.U=G&amp;U:7&gt;Z,G.U&lt;!!51!%,35]A5X2S982F:XE!%E!Q`````QB73%2-4G&amp;N:1!!1!$RPAH!FA!!!!%:2GFG&lt;V^"=G*0=(2J&lt;WZT5X2S;7ZH,G.U&lt;!!?1$$`````%&amp;*F971A18*C)%^Q&gt;'FP&lt;H-!!%!!]&lt;Y*Q*9!!!!"'5:J:G^@18*C4X"U;7^O=V.U=GFO:SZD&gt;'Q!(E!Q`````R&amp;8=GFU:3""=G)A4X"U;7^O=Q!L!0%!!!!!!!!!!2"';7:P8UVF&lt;62Z='5O9X2M!"&amp;!!AB315UA&gt;(FQ:1!!%%!(#U2.13"$;'&amp;O&lt;G6M!!R!)1:8=GFU:4]!!!J!)16-&lt;W.B&lt;!"&amp;!0'`[U5V!!!!!1Z';7:P8V.U982F,G.U&lt;!!N1&amp;!!$!!!!!%!!A!$!!1!"1!'!!=!#!!*!!I!#QJ';7:P)&amp;.U982F!!!"!!Q!!!!+15EQ2'VB2EF'4Q!!$`]!"!!!!!!"!!!!.%&amp;*-%2N95:*2E^@-41U-D%Z-T%R.$=X.T1S.$!R.DER/$=Z-4)U-4%Z-49Z-4!S-4-Y.41!!!!44X"U;7VJ?G5A2G^S)&amp;.J&lt;G&gt;M:1!!!".0=(2J&lt;7F[:3"'&lt;X)A5WFO:WRF!!)!!!!!!1!!!!!!</Property>
            <Property Name="fifo.version" Type="UInt">5</Property>
            <Property Name="FPGA.PersistentID" Type="Str">{8F567677-804D-4DD5-BF28-47F23E53C5BA}</Property>
         </Item>
         <Item Name="AI1DmaFIFO" Type="FPGA FIFO">
            <Property Name="fifo.configuration" Type="Str">"Depth=4095;Width=4;Dir=0;Strategy=1;Read Arbs=Optimize For Single;Write Arbs=Optimize For Single;Type=2;Channel=1;Write=1AI1DmaFIFO"</Property>
            <Property Name="fifo.configured" Type="Bool">true</Property>
            <Property Name="fifo.state" Type="Bin">###!!A!!!!U!$E!Q`````Q2/97VF!!!+1!=&amp;2'6Q&gt;'A!.1$R!!!!!!!!!!%92GFG&lt;V^%982B6(FQ:5.P&lt;H2S&lt;WQO9X2M!".!!AJ%982B)&amp;&gt;J:(2I!!!R!0%!!!!!!!!!!2*';7:P8U2J=G6D&gt;'FP&lt;CZD&gt;'Q!&amp;5!$$5:*2E^%;8*F9X2J&lt;WY!-!$R!!!!!!!!!!%42GFG&lt;V^*4V.U=G&amp;U:7&gt;Z,G.U&lt;!!51!%,35]A5X2S982F:XE!%E!Q`````QB73%2-4G&amp;N:1!!1!$RPAH!FA!!!!%:2GFG&lt;V^"=G*0=(2J&lt;WZT5X2S;7ZH,G.U&lt;!!?1$$`````%&amp;*F971A18*C)%^Q&gt;'FP&lt;H-!!%!!]&lt;Y*Q*9!!!!"'5:J:G^@18*C4X"U;7^O=V.U=GFO:SZD&gt;'Q!(E!Q`````R&amp;8=GFU:3""=G)A4X"U;7^O=Q!L!0%!!!!!!!!!!2"';7:P8UVF&lt;62Z='5O9X2M!"&amp;!!AB315UA&gt;(FQ:1!!%%!(#U2.13"$;'&amp;O&lt;G6M!!R!)1:8=GFU:4]!!!J!)16-&lt;W.B&lt;!"&amp;!0'`[U5V!!!!!1Z';7:P8V.U982F,G.U&lt;!!N1&amp;!!$!!!!!%!!A!$!!1!"1!'!!=!#!!*!!I!#QJ';7:P)&amp;.U982F!!!"!!Q!!!!+15ER2'VB2EF'4Q!!$`]!"!!!!!!"!!!!.%&amp;*-52N95:*2E^@-4AW.T%Q/$1X-D)S.D)T-4%Z.$!R-4AR.41R.4-R/$%S.$)R-T-S-45!!!!44X"U;7VJ?G5A2G^S)&amp;.J&lt;G&gt;M:1!!!".0=(2J&lt;7F[:3"'&lt;X)A5WFO:WRF!!)!!!!"!1!!!!!!</Property>
            <Property Name="fifo.version" Type="UInt">5</Property>
            <Property Name="FPGA.PersistentID" Type="Str">{7DE9DEAA-991D-4D97-83D7-2AA568C60332}</Property>
         </Item>
         <Item Name="ctempDmaFIFO" Type="FPGA FIFO">
            <Property Name="fifo.configuration" Type="Str">"Depth=4095;Width=4;Dir=0;Strategy=1;Read Arbs=Optimize For Single;Write Arbs=Optimize For Single;Type=2;Channel=2;Write=1ctempDmaFIFO"</Property>
            <Property Name="fifo.configured" Type="Bool">true</Property>
            <Property Name="fifo.state" Type="Bin">###!!A!!!!U!$E!Q`````Q2/97VF!!!+1!=&amp;2'6Q&gt;'A!.1$R!!!!!!!!!!%92GFG&lt;V^%982B6(FQ:5.P&lt;H2S&lt;WQO9X2M!".!!AJ%982B)&amp;&gt;J:(2I!!!R!0%!!!!!!!!!!2*';7:P8U2J=G6D&gt;'FP&lt;CZD&gt;'Q!&amp;5!$$5:*2E^%;8*F9X2J&lt;WY!-!$R!!!!!!!!!!%42GFG&lt;V^*4V.U=G&amp;U:7&gt;Z,G.U&lt;!!51!%,35]A5X2S982F:XE!%E!Q`````QB73%2-4G&amp;N:1!!1!$RPAH!FA!!!!%:2GFG&lt;V^"=G*0=(2J&lt;WZT5X2S;7ZH,G.U&lt;!!?1$$`````%&amp;*F971A18*C)%^Q&gt;'FP&lt;H-!!%!!]&lt;Y*Q*9!!!!"'5:J:G^@18*C4X"U;7^O=V.U=GFO:SZD&gt;'Q!(E!Q`````R&amp;8=GFU:3""=G)A4X"U;7^O=Q!L!0%!!!!!!!!!!2"';7:P8UVF&lt;62Z='5O9X2M!"&amp;!!AB315UA&gt;(FQ:1!!%%!(#U2.13"$;'&amp;O&lt;G6M!!R!)1:8=GFU:4]!!!J!)16-&lt;W.B&lt;!"&amp;!0'`[U5V!!!!!1Z';7:P8V.U982F,G.U&lt;!!N1&amp;!!$!!!!!%!!A!$!!1!"1!'!!=!#!!*!!I!#QJ';7:P)&amp;.U982F!!!"!!Q!!!!-9X2F&lt;8"%&lt;7&amp;'35:0!!!0`Q!%!!!!!!%!!!!U9X2F&lt;8"%&lt;7&amp;'35:08T)S-4%Z.T%Z/$)Q.$=W/$=Q-4EX-D-U.D9Z-T%Y-D)T.D5V/$-S.Q!!!".0=(2J&lt;7F[:3"'&lt;X)A5WFO:WRF!!!!%U^Q&gt;'FN;8JF)%:P=C"4;7ZH&lt;'5!!A!!!!)"!!!!!!!</Property>
            <Property Name="fifo.version" Type="UInt">5</Property>
            <Property Name="FPGA.PersistentID" Type="Str">{398E439F-2F82-4D4D-BFC5-4849016FE4DD}</Property>
         </Item>
         <Item Name="Dependencies" Type="Dependencies"/>
         <Item Name="Build Specifications" Type="Build"/>
      </Item>
      <Item Name="Vaisala parse.vi" Type="VI" URL="Vaisala parse.vi"/>
      <Item Name="Debug check and send.vi" Type="VI" URL="Debug check and send.vi"/>
      <Item Name="Dependencies" Type="Dependencies">
         <Item Name="vi.lib" Type="Folder">
            <Item Name="TCP Listen.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/tcp.llb/TCP Listen.vi"/>
            <Item Name="Internecine Avoider.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/tcp.llb/Internecine Avoider.vi"/>
            <Item Name="TCP Listen List Operations.ctl" Type="VI" URL="/&lt;vilib&gt;/Utility/tcp.llb/TCP Listen List Operations.ctl"/>
            <Item Name="TCP Listen Internal List.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/tcp.llb/TCP Listen Internal List.vi"/>
            <Item Name="General Error Handler.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/General Error Handler.vi"/>
            <Item Name="DialogType.ctl" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/DialogType.ctl"/>
            <Item Name="DialogTypeEnum.ctl" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/DialogTypeEnum.ctl"/>
            <Item Name="General Error Handler CORE.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/General Error Handler CORE.vi"/>
            <Item Name="Check Special Tags.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Check Special Tags.vi"/>
            <Item Name="TagReturnType.ctl" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/TagReturnType.ctl"/>
            <Item Name="Set String Value.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Set String Value.vi"/>
            <Item Name="GetRTHostConnectedProp.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/GetRTHostConnectedProp.vi"/>
            <Item Name="Error Code Database.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Error Code Database.vi"/>
            <Item Name="whitespace.ctl" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/whitespace.ctl"/>
            <Item Name="Trim Whitespace.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Trim Whitespace.vi"/>
            <Item Name="Format Message String.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Format Message String.vi"/>
            <Item Name="Find Tag.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Find Tag.vi"/>
            <Item Name="Search and Replace Pattern.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Search and Replace Pattern.vi"/>
            <Item Name="Set Bold Text.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Set Bold Text.vi"/>
            <Item Name="Details Display Dialog.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Details Display Dialog.vi"/>
            <Item Name="ErrWarn.ctl" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/ErrWarn.ctl"/>
            <Item Name="Clear Errors.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Clear Errors.vi"/>
            <Item Name="eventvkey.ctl" Type="VI" URL="/&lt;vilib&gt;/event_ctls.llb/eventvkey.ctl"/>
            <Item Name="Not Found Dialog.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Not Found Dialog.vi"/>
            <Item Name="Three Button Dialog.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Three Button Dialog.vi"/>
            <Item Name="Three Button Dialog CORE.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Three Button Dialog CORE.vi"/>
            <Item Name="Longest Line Length in Pixels.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Longest Line Length in Pixels.vi"/>
            <Item Name="Convert property node font to graphics font.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Convert property node font to graphics font.vi"/>
            <Item Name="Get Text Rect.vi" Type="VI" URL="/&lt;vilib&gt;/picture/picture.llb/Get Text Rect.vi"/>
            <Item Name="BuildHelpPath.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/BuildHelpPath.vi"/>
            <Item Name="GetHelpDir.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/GetHelpDir.vi"/>
            <Item Name="Create Semaphore.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/semaphor.llb/Create Semaphore.vi"/>
            <Item Name="Validate Semaphore Size.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/semaphor.llb/Validate Semaphore Size.vi"/>
            <Item Name="Error Cluster From Error Code.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Error Cluster From Error Code.vi"/>
            <Item Name="Semaphore Core.vi" Type="VI" URL="/&lt;vilib&gt;/Platform/synch.llb/Semaphore Core.vi"/>
            <Item Name="Semaphore Action.ctl" Type="VI" URL="/&lt;vilib&gt;/Utility/semaphor.llb/Semaphore Action.ctl"/>
            <Item Name="Semaphore Size DB.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/semaphor.llb/Semaphore Size DB.vi"/>
            <Item Name="Open Config Data.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Open Config Data.vi"/>
            <Item Name="Config Data Open Reference.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Config Data Open Reference.vi"/>
            <Item Name="Config Data Registry Functions.ctl" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Config Data Registry Functions.ctl"/>
            <Item Name="Config Data Registry.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Config Data Registry.vi"/>
            <Item Name="Config Data.ctl" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Config Data.ctl"/>
            <Item Name="Config Data Section.ctl" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Config Data Section.ctl"/>
            <Item Name="Config Data Set File Path.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Config Data Set File Path.vi"/>
            <Item Name="Config Data Modify Functions.ctl" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Config Data Modify Functions.ctl"/>
            <Item Name="Config Data Modify.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Config Data Modify.vi"/>
            <Item Name="Add Quotes.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Add Quotes.vi"/>
            <Item Name="Info From Config Data.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Info From Config Data.vi"/>
            <Item Name="Config Data Read From File.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Config Data Read From File.vi"/>
            <Item Name="Config Data Get File Path.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Config Data Get File Path.vi"/>
            <Item Name="String to Config Data.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/String to Config Data.vi"/>
            <Item Name="Invalid Config Data Reference.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Invalid Config Data Reference.vi"/>
            <Item Name="Config Data Close Reference.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Config Data Close Reference.vi"/>
            <Item Name="Read Key.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Read Key.vi"/>
            <Item Name="Read Key (Boolean).vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Read Key (Boolean).vi"/>
            <Item Name="Config Data Get Key Value.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Config Data Get Key Value.vi"/>
            <Item Name="Read Key (Double).vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Read Key (Double).vi"/>
            <Item Name="Read Key (I32).vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Read Key (I32).vi"/>
            <Item Name="Read Key (Path).vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Read Key (Path).vi"/>
            <Item Name="Remove Quotes.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Remove Quotes.vi"/>
            <Item Name="Common Path to Specific Path.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Common Path to Specific Path.vi"/>
            <Item Name="Read Key (String).vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Read Key (String).vi"/>
            <Item Name="Parse Stored String.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Parse Stored String.vi"/>
            <Item Name="Get Next Character.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Get Next Character.vi"/>
            <Item Name="Read Key (U32).vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Read Key (U32).vi"/>
            <Item Name="Close Config Data.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Close Config Data.vi"/>
            <Item Name="Config Data Write To File.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Config Data Write To File.vi"/>
            <Item Name="Config Data to String.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Config Data to String.vi"/>
            <Item Name="Merge Errors.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Merge Errors.vi"/>
            <Item Name="Destroy Semaphore.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/semaphor.llb/Destroy Semaphore.vi"/>
            <Item Name="Serial Port Read.vi" Type="VI" URL="/&lt;vilib&gt;/Instr/Serial.llb/Serial Port Read.vi"/>
            <Item Name="Open Serial Driver.vi" Type="VI" URL="/&lt;vilib&gt;/Instr/_sersup.llb/Open Serial Driver.vi"/>
            <Item Name="serpConfig.vi" Type="VI" URL="/&lt;vilib&gt;/Instr/Serial.llb/serpConfig.vi"/>
            <Item Name="Serial Port Write.vi" Type="VI" URL="/&lt;vilib&gt;/Instr/Serial.llb/Serial Port Write.vi"/>
            <Item Name="Serial Port Init.vi" Type="VI" URL="/&lt;vilib&gt;/Instr/Serial.llb/Serial Port Init.vi"/>
            <Item Name="VISA Configure Serial Port" Type="Document" URL="/&lt;vilib&gt;/Instr/_visa.llb/VISA Configure Serial Port"/>
            <Item Name="VISA Configure Serial Port (Instr).vi" Type="VI" URL="/&lt;vilib&gt;/Instr/_visa.llb/VISA Configure Serial Port (Instr).vi"/>
            <Item Name="VISA Configure Serial Port (Serial Instr).vi" Type="VI" URL="/&lt;vilib&gt;/Instr/_visa.llb/VISA Configure Serial Port (Serial Instr).vi"/>
            <Item Name="serial line ctrl.vi" Type="VI" URL="/&lt;vilib&gt;/Instr/_sersup.llb/serial line ctrl.vi"/>
            <Item Name="Bytes At Serial Port.vi" Type="VI" URL="/&lt;vilib&gt;/Instr/Serial.llb/Bytes At Serial Port.vi"/>
            <Item Name="Get Channel Information.vi" Type="VI" URL="/&lt;vilib&gt;/DAQ/DqChnUtl.llb/Get Channel Information.vi"/>
            <Item Name="Write Key.vi" Type="VI" URL="/&lt;vilib&gt;/UTILITY/config.llb/Write Key.vi"/>
            <Item Name="Write Key (Boolean).vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Write Key (Boolean).vi"/>
            <Item Name="Write Key (Double).vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Write Key (Double).vi"/>
            <Item Name="Write Key (I32).vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Write Key (I32).vi"/>
            <Item Name="Write Key (Path).vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Write Key (Path).vi"/>
            <Item Name="Specific Path to Common Path.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Specific Path to Common Path.vi"/>
            <Item Name="Write Key (String).vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Write Key (String).vi"/>
            <Item Name="Single to Double Backslash.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Single to Double Backslash.vi"/>
            <Item Name="Remove Unprintable Chars.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Remove Unprintable Chars.vi"/>
            <Item Name="Write Key (U32).vi" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Write Key (U32).vi"/>
            <Item Name="Simple Error Handler.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Simple Error Handler.vi"/>
            <Item Name="AI Sample Channels.vi" Type="VI" URL="/&lt;vilib&gt;/DAQ/1EASYIO.LLB/AI Sample Channels.vi"/>
            <Item Name="AI Sample Channels (single-point waveform).vi" Type="VI" URL="/&lt;vilib&gt;/DAQ/1EASYIO.LLB/AI Sample Channels (single-point waveform).vi"/>
            <Item Name="Acquire Semaphore.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/semaphor.llb/Acquire Semaphore.vi"/>
            <Item Name="Find First Error.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/error.llb/Find First Error.vi"/>
            <Item Name="Release Semaphore_71.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/semaphor.llb/Release Semaphore_71.vi"/>
            <Item Name="Get Semaphore Status.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/semaphor.llb/Get Semaphore Status.vi"/>
            <Item Name="Write Characters To File.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/file.llb/Write Characters To File.vi"/>
            <Item Name="Open_Create_Replace File.vi" Type="VI" URL="/&lt;vilib&gt;/_oldvers/_oldvers.llb/Open_Create_Replace File.vi"/>
            <Item Name="compatFileDialog.vi" Type="VI" URL="/&lt;vilib&gt;/_oldvers/_oldvers.llb/compatFileDialog.vi"/>
            <Item Name="compatOpenFileOperation.vi" Type="VI" URL="/&lt;vilib&gt;/_oldvers/_oldvers.llb/compatOpenFileOperation.vi"/>
            <Item Name="compatCalcOffset.vi" Type="VI" URL="/&lt;vilib&gt;/_oldvers/_oldvers.llb/compatCalcOffset.vi"/>
            <Item Name="Write File+ (string).vi" Type="VI" URL="/&lt;vilib&gt;/Utility/file.llb/Write File+ (string).vi"/>
            <Item Name="compatWriteText.vi" Type="VI" URL="/&lt;vilib&gt;/_oldvers/_oldvers.llb/compatWriteText.vi"/>
            <Item Name="Close File+.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/file.llb/Close File+.vi"/>
            <Item Name="Open/Create/Replace File.vi" Type="VI" URL="/&lt;vilib&gt;/Utility/file.llb/Open/Create/Replace File.vi"/>
            <Item Name="subDisplayMessage.vi" Type="VI" URL="/&lt;vilib&gt;/express/express output/DisplayMessageBlock.llb/subDisplayMessage.vi"/>
            <Item Name="ex_CorrectErrorChain.vi" Type="VI" URL="/&lt;vilib&gt;/express/express shared/ex_CorrectErrorChain.vi"/>
            <Item Name="Config Data RefNum" Type="VI" URL="/&lt;vilib&gt;/Utility/config.llb/Config Data RefNum"/>
            <Item Name="Semaphore RefNum" Type="VI" URL="/&lt;vilib&gt;/Utility/semaphor.llb/Semaphore RefNum"/>
            <Item Name="LVDateTimeRec.ctl" Type="VI" URL="/&lt;vilib&gt;/Utility/miscctls.llb/LVDateTimeRec.ctl"/>
            <Item Name="RT Set Date and Time.vi" Type="VI" URL="../../../../../../../Program Files/National Instruments/LabVIEW 8.5/Targets/NI/RT/vi.lib/rtutility.llb/RT Set Date and Time.vi"/>
            <Item Name="settime.dll" Type="Document" URL="../../../../../../../Program Files/National Instruments/LabVIEW 8.5/Targets/NI/RT/vi.lib/settime.dll"/>
            <Item Name="Application Version.ctl" Type="VI" URL="/&lt;vilib&gt;/Utility/libraryn.llb/Application Version.ctl"/>
            <Item Name="nirio_resource_hc.ctl" Type="VI" URL="/&lt;vilib&gt;/userDefined/High Color/nirio_resource_hc.ctl"/>
         </Item>
         <Item Name="Global variables NTCP.vi" Type="VI" URL="ntcp-subroutines.llb/Global variables NTCP.vi"/>
         <Item Name="Open ntcp command queue.vi" Type="VI" URL="ntcp-subroutines.llb/Open ntcp command queue.vi"/>
         <Item Name="Open return queue.vi" Type="VI" URL="ntcp-subroutines.llb/Open return queue.vi"/>
         <Item Name="ntcp cmd - parse and enqueue.vi" Type="VI" URL="ntcp-subroutines.llb/ntcp cmd - parse and enqueue.vi"/>
         <Item Name="ntcp command dispatcher.vi" Type="VI" URL="ntcp-subroutines.llb/ntcp command dispatcher.vi"/>
         <Item Name="NiRioSrv.dll" Type="Document" URL="NiRioSrv.dll"/>
         <Item Name="semaphor" Type="VI" URL="semaphor"/>
         <Item Name="_nirio_device_handleType.ctl" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_driverPrimitives.llb/_nirio_device_handleType.ctl"/>
         <Item Name="_nirio_device_close.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_driverPrimitives.llb/_nirio_device_close.vi"/>
         <Item Name="_nirio_device_attributes.ctl" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_driverPrimitives.llb/_nirio_device_attributes.ctl"/>
         <Item Name="_nirio_device_attrSet32.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_driverPrimitives.llb/_nirio_device_attrSet32.vi"/>
         <Item Name="nirio_Close.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_HostInterface/nirio_Close.vi"/>
         <Item Name="nirviIntfClose_cRIO-9104.vi" Type="VI" URL="/&lt;vilib&gt;/FPGAPlugInAG/cRIO-9104/nirviIntfClose_cRIO-9104.vi"/>
         <Item Name="nirio_DMARead.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_HostInterface/nirio_DMARead.vi"/>
         <Item Name="nirio_EnableInterrupts.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_HostInterface/nirio_EnableInterrupts.vi"/>
         <Item Name="niLvFpgaErrorClusterFromErrorCode.vi" Type="VI" URL="/&lt;vilib&gt;/rvi/errors/niLvFpgaErrorClusterFromErrorCode.vi"/>
         <Item Name="nirviErrorClusterFromErrorCode.vi" Type="VI" URL="/&lt;vilib&gt;/RVI Host/nirviSupport.llb/nirviErrorClusterFromErrorCode.vi"/>
         <Item Name="_nirio_device_sessionStates.ctl" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_driverPrimitives.llb/_nirio_device_sessionStates.ctl"/>
         <Item Name="_nirio_device_attributesString.ctl" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_driverPrimitives.llb/_nirio_device_attributesString.ctl"/>
         <Item Name="_nirio_device_attrSetString.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_driverPrimitives.llb/_nirio_device_attrSetString.vi"/>
         <Item Name="nirio_CleanUpAfterDownload.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_HostInterface/nirio_CleanUpAfterDownload.vi"/>
         <Item Name="_nirio_device_configSet.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_driverPrimitives.llb/_nirio_device_configSet.vi"/>
         <Item Name="Fifo_DMA_Config.ctl" Type="VI" URL="/&lt;vilib&gt;/rvi/FIFO/Fifo_Types/Fifo_DMA_Config.ctl"/>
         <Item Name="nirio_DMAReconfigureDriver.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_HostInterface/nirio_DMAReconfigureDriver.vi"/>
         <Item Name="nirio_ConfigureRegisterAddresses.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_HostInterface/nirio_ConfigureRegisterAddresses.vi"/>
         <Item Name="_nirio_device_writeBlock32.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_driverPrimitives.llb/_nirio_device_writeBlock32.vi"/>
         <Item Name="nirio_Read32.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_HostInterface/nirio_Read32.vi"/>
         <Item Name="nirio_Write32.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_HostInterface/nirio_Write32.vi"/>
         <Item Name="_nirio_device_writeBlock8.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_driverPrimitives.llb/_nirio_device_writeBlock8.vi"/>
         <Item Name="_nirio_device_writeBlock16.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_driverPrimitives.llb/_nirio_device_writeBlock16.vi"/>
         <Item Name="_nirio_device_writeBlock.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_driverPrimitives.llb/_nirio_device_writeBlock.vi"/>
         <Item Name="nirio_Read8.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_HostInterface/nirio_Read8.vi"/>
         <Item Name="nirio_Write8.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_HostInterface/nirio_Write8.vi"/>
         <Item Name="_nirio_device_attrGet32.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_driverPrimitives.llb/_nirio_device_attrGet32.vi"/>
         <Item Name="nirio_Download.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_HostInterface/nirio_Download.vi"/>
         <Item Name="nirio_DisableInterrupts.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_HostInterface/nirio_DisableInterrupts.vi"/>
         <Item Name="nirio_DMAStopAll.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_HostInterface/nirio_DMAStopAll.vi"/>
         <Item Name="nirio_MultilineStringToArray.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_Utility/nirio_MultilineStringToArray.vi"/>
         <Item Name="_nirio_device_attrGetString.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_driverPrimitives.llb/_nirio_device_attrGetString.vi"/>
         <Item Name="nirio_IsItOKToDownload.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_HostInterface/nirio_IsItOKToDownload.vi"/>
         <Item Name="_nirio_device_readBlock32.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_driverPrimitives.llb/_nirio_device_readBlock32.vi"/>
         <Item Name="nirviFillInErrorInfo.vi" Type="VI" URL="/&lt;vilib&gt;/rvi/errors/nirviFillInErrorInfo.vi"/>
         <Item Name="nirviReportUnexpectedCaseInternalError (String).vi" Type="VI" URL="/&lt;vilib&gt;/rvi/errors/nirviReportUnexpectedCaseInternalError (String).vi"/>
         <Item Name="nirviReportUnexpectedCaseInternalError (U32).vi" Type="VI" URL="/&lt;vilib&gt;/rvi/errors/nirviReportUnexpectedCaseInternalError (U32).vi"/>
         <Item Name="nirviReportUnexpectedCaseInternalError (Bool).vi" Type="VI" URL="/&lt;vilib&gt;/rvi/errors/nirviReportUnexpectedCaseInternalError (Bool).vi"/>
         <Item Name="nirviReportUnexpectedCaseInternalError.vi" Type="VI" URL="/&lt;vilib&gt;/rvi/errors/nirviReportUnexpectedCaseInternalError.vi"/>
         <Item Name="nirio_PrepareForDownload.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_HostInterface/nirio_PrepareForDownload.vi"/>
         <Item Name="nirviRIOSetUpMiniMite.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_HostInterface/nirviRIOSetUpMiniMite.vi"/>
         <Item Name="nirio_AppVersionToI32.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_Utility/nirio_AppVersionToI32.vi"/>
         <Item Name="nirio_CheckDriverVersion.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_Utility/nirio_CheckDriverVersion.vi"/>
         <Item Name="_nirio_device_open.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_driverPrimitives.llb/_nirio_device_open.vi"/>
         <Item Name="nirio_Open.vi" Type="VI" URL="/&lt;vilib&gt;/LabVIEW Targets/FPGA/RIO/nirio_HostInterface/nirio_Open.vi"/>
         <Item Name="nirviWhatTheDeviceIsDoing.ctl" Type="VI" URL="/&lt;vilib&gt;/rvi/ClientSDK/nirviWhatTheDeviceIsDoing.ctl"/>
         <Item Name="niFPGADownloadSettings.ctl" Type="VI" URL="/&lt;vilib&gt;/rvi/interface/stock/niFPGADownloadSettings.ctl"/>
         <Item Name="nirviIntfOpen_cRIO-9104.vi" Type="VI" URL="/&lt;vilib&gt;/FPGAPlugInAG/cRIO-9104/nirviIntfOpen_cRIO-9104.vi"/>
      </Item>
      <Item Name="Build Specifications" Type="Build"/>
   </Item>
</Project>
