%global eclipse_base    %{_libdir}/eclipse
%global install_loc     %{_datadir}/eclipse/dropins
# Taken from update site so we match upstream
%global qualifier       v201107081312

Name:           eclipse-bzip2
Version:        1.0.0
Release:        0%{?dist}
Summary:        Eclipse bzip2 project import/export plugin
License:        EPL
#URL:            http://
Source0:        eclipse-bzip2-1.0.0.tar.gz

BuildRequires:  java-devel >= 1:1.6.0
BuildRequires:  eclipse-platform >= 1:3.7.0
BuildRequires:  eclipse-pde >= 1:3.4.0 
BuildRequires:  apache-commons-compress
Requires:    	eclipse-platform >= 1:3.7.0
Requires:       apache-commons-compress

BuildArch:		noarch

%description
Eclipse plugin for Bzip2 project import/export.

%prep
rm -rf eclipse-bzip2-%{version}
%setup -q -c eclipse-bzip2-%{version}
%{__rm} -rf orbitDeps
mkdir orbitDeps
pushd orbitDeps
ln -s %{_javadir}/apache-commons-compress.jar
#ln -s %{eclipse_base}/dropins/jdt/plugins/org.eclipse.jdt.core_*.jar
popd

%build
%{eclipse_base}/buildscripts/pdebuild -f org.eclipse.ui.ide.format.extension.plugin.feature \
	-a "-DjavacSource=1.6 -DjavacTarget=1.6 -DforceContextQualifier=%{qualifier}" \
    -o `pwd`/orbitDeps -d 'jdt sdk'

%install
install -d -m 755 %{buildroot}%{_datadir}/eclipse
install -d -m 755 %{buildroot}%{install_loc}/eclipse-bzip2

# eclipse-bzip2 main feature
unzip -q -d %{buildroot}%{install_loc}/eclipse-bzip2 \
	build/rpmBuild/org.eclipse.ui.ide.format.extension.plugin.feature.zip
pushd %{buildroot}%{install_loc}/eclipse-bzip2/eclipse/plugins
# org.apache.commons.compress_1.1.0.jar	
rm org.apache.commons.compress*.jar
#rm org.eclipse.jdt.core_*.jar
ln -s %{_javadir}/apache-commons-compress.jar
popd

%files
%defattr(-,root,root,-)
%{install_loc}/eclipse-bzip2
%doc eclipse-bzip2-1.0.0/org.eclipse.ui.ide.format.extension.plugin.feature/epl-v10.html
%doc eclipse-bzip2-1.0.0/org.eclipse.ui.ide.format.extension.plugin.feature/license.html

%changelog
* Fri Jul 8 2011 Kiu Kwan Leung <kleung@redhat.com> 1.0.0-0
- Initial Release