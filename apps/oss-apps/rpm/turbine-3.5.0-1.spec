# Copyright (c) 2005, 2006, Lawrence J. Miller and NEESit
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
#   * Redistributions of source code must retain the above copyright notice,
# this list of conditions and the following disclaimer.
#   * Redistributions in binary form must reproduce the above copyright
# notice, this list of conditions and the following disclaimer in the 
# documentation and/or other materials provided with the distribution.
#   * Neither the name of the San Diego Supercomputer Center nor the names of
# its contributors may be used to endorse or promote products derived from this
# software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.

Summary: The NEESit turbine package contains a Java archive of programs that are utility applications to the RBNB dynamic data server made by Creare that are useful for real-time data streaming. http://outlet.creare.com/rbnb/
Name: turbine
Version: 3.5.0
Release: 1
License: BSD
Group: Applications/Engineering
Vendor: NEESit
Source: %{name}-%{version}.zip
URL: http://it.nees.org
packager: Lawrence J. Miller <ljmiller@sdsc.edu>

BuildRoot: /tmp/%{name}-%{version}
BuildArch: noarch

# define installation targets
%define TURBINE_DIR /usr/share/java
%define PROFILE_DIR /etc/profile.d
%define DOC_DIR /usr/share/javadoc/%{name}-%{version}

#Autoreqprov: no
Requires: java-1.5.0-sun-compat ant rbnb

%description
The NEESit turbine package contains a Java archive of programs that are utility
 applications to the RBNB dynamic data server made by Creare that are useful 
 for real-time data streaming. 
 http://outlet.creare.com/rbnb/

%prep
%setup -q

%build
ant clean doc jar

#####################################################
%install
rm -rf $RPM_BUILD_ROOT

#make base dirs
mkdir -p $RPM_BUILD_ROOT/%{TURBINE_DIR}
mkdir -p $RPM_BUILD_ROOT/%{PROFILE_DIR}
mkdir -p $RPM_BUILD_ROOT/%{DOC_DIR}

#install turbine.jar
install -o root -m 644 build/lib/turbine-%{version}.jar $RPM_BUILD_ROOT/%{TURBINE_DIR}

#install profile
install -m 644 rpm/etc/profile.d/turbine.sh $RPM_BUILD_ROOT/%{PROFILE_DIR}
install -m 644 rpm/etc/profile.d/turbine.csh $RPM_BUILD_ROOT/%{PROFILE_DIR}

#install docs
install -d -o root -m 644 docs $RPM_BUILD_ROOT/%{DOC_DIR}

%clean
rm -rf $RPM_BUILD_ROOT

%files
%{TURBINE_DIR}/turbine-%{version}.jar
%{PROFILE_DIR}/turbine.csh
%{PROFILE_DIR}/turbine.sh
%dir %{DOC_DIR}

%config
%defattr(0644, root, root)
%{TURBINE_DIR}/turbine-%{version}.jar
%defattr(0644, root, root)
%{PROFILE_DIR}/turbine.csh
%defattr(0644, root, root)
%{PROFILE_DIR}/turbine.sh
%defattr(0755, root, root)
%dir %{DOC_DIR}

%pre

%post

%preun

%postun
