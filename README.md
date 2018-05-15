## onos-apps

### 1. why creates onos-apps

During the time of researching in SDN, it's oblivious to use SDN controller and switch. My research mainly focus on ONOS 1.11 
and POFswitch or OVS-POF, both of which are modified to support POF protocol. I change the OVS, which is based on OVS 2.5, to 
support POF. Then, it should take more tests to monitor the OVS-POF function. 

There are no mistakes between pox and OVS-POF for now. The next work I will do is to test the inter-communicate between ONOS 
and OVS-POF. This repository will store my created apps. Note that, those apps may not just all act as test apps.

The other apps I have ever committed can be found also in my git:

- [ONOS-source-routing](https://github.com/iflytang/ONOS-source-routing)
- [ONOS-byon](https://githubcom/iflytang/ONOS-byon)
- [ONOS-mobility-management-optimalmode](https://githubcom/iflytang/ONOS-mobility-management-optimalmode)

### 2. How to run onos-apps

Here, I will show you instructions how to run these onos apps. And I assume that you have already run the ONOS controller. There
are also two documents that can be referenced, see:

- [ONOS buck build](https://docs.google.com/document/d/1hAqBDFry2f4w9lMCAY_ieO04nWLR4gHV0uJrgV-8ovE/edit#heading=h.69a07zqxoy0r)
- [ONOS wiki: Template Application Toturial](https://wiki.onosproject.org/display/ONOS/Template+Application+Tutorial)

Then, follow the instructions as followed:

- clone the source code from git
```
git clone git@github.com:iflytang/onos-apps.git
```

- compile and install

First, come into the sub directory, for example, ```cd pof-ovs-app```. Then run:

```
# compile to create *.oar file
1. mvn clean install 

# install *.oar file to ONOS controller
2. usage: onos-app <node-ip> list
          onos-app <node-ip> {install|install!} <app-file>
          onos-app <node-ip> {reinstall|reinstall!} [<app-name>] <app-file>
          onos-app <node-ip> {activate|deactivate|uninstall} <app-name>
```


