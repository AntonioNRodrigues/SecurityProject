#!/bin/sh
MyGitClient=10.101.148.4
Gcc=10.101.151.5
export MyGitClient
export Gcc
sudo /sbin/iptables -A INPUT -s 10.101.253.11 -j ACCEPT 
sudo /sbin/iptables -A INPUT -s 10.101.253.12 -j ACCEPT 
sudo /sbin/iptables -A INPUT -s 10.101.253.13 -j ACCEPT 
sudo /sbin/iptables -A INPUT -s 10.121.53.14 -j ACCEPT 
sudo /sbin/iptables -A INPUT -s 10.121.53.15 -j ACCEPT 
sudo /sbin/iptables -A INPUT -s 10.101.53.16 -j ACCEPT 
sudo /sbin/iptables -A INPUT -s 10.101.249.63 -j ACCEPT 
sudo /sbin/iptables -A INPUT -s 10.101.85.6 -j ACCEPT 
sudo /sbin/iptables -A INPUT -s 10.101.85.138 -j ACCEPT 
sudo /sbin/iptables -A INPUT -s 10.101.85.18 -j ACCEPT 
sudo /sbin/iptables -A INPUT -s 10.101.148.1 -j ACCEPT 
sudo /sbin/iptables -A INPUT -s 10.101.85.134 -j ACCEPT
sudo /sbin/iptables -A INPUT -s $MyGitClient/23 -p icmp -j ACCEPT
sudo /sbin/iptables -A INPUT -s $MyGitClient/23 -p tcp --dport 22 -j ACCEPT
sudo /sbin/iptables -A INPUT -p tcp --dport 4567 -j ACCEPT
sudo /sbin/iptables -A INPUT -i lo -j ACCEPT
sudo /sbin/iptables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT
sudo /sbin/iptables -A INPUT -j DROP 
sudo /sbin/iptables -A OUTPUT -d 10.101.253.11 -j ACCEPT 
sudo /sbin/iptables -A OUTPUT -d 10.101.253.12 -j ACCEPT 
sudo /sbin/iptables -A OUTPUT -d 10.101.253.13 -j ACCEPT 
sudo /sbin/iptables -A OUTPUT -d 10.121.53.14 -j ACCEPT 
sudo /sbin/iptables -A OUTPUT -d 10.121.53.15 -j ACCEPT 
sudo /sbin/iptables -A OUTPUT -d 10.101.53.16 -j ACCEPT 
sudo /sbin/iptables -A OUTPUT -d 10.101.249.63 -j ACCEPT 
sudo /sbin/iptables -A OUTPUT -d 10.101.85.6 -j ACCEPT 
sudo /sbin/iptables -A OUTPUT -d 10.101.85.138  -j ACCEPT 
sudo /sbin/iptables -A OUTPUT -d 10.101.85.18 -j ACCEPT 
sudo /sbin/iptables -A OUTPUT -d 10.101.148.1 -j ACCEPT 
sudo /sbin/iptables -A OUTPUT -d 10.101.85.134 -j ACCEPT 
sudo /sbin/iptables -A OUTPUT -m state --state ESTABLISHED,RELATED -j ACCEPT
sudo /sbin/iptables -A OUTPUT -d $Gcc -p icmp -j ACCEPT
sudo /sbin/iptables -A OUTPUT -d $MyGitClient/23 -p tcp --dport 22 -j ACCEPT
sudo /sbin/iptables -A OUTPUT -o lo -j ACCEPT 
sudo /sbin/iptables -A OUTPUT -d $MyGitClient/23 -p tcp --dport 4567 -j ACCEPT
sudo /sbin/iptables -A OUTPUT -j DROP
