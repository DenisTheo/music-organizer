<?xml version="1.0" encoding="UTF-8"?>
<playlist version="1" xmlns="http://xspf.org/ns/0/">
    <trackList>
    	<#list links as link>
        	<track>
	            <location>${link}</location>
	        </track>
        </#list>
    </trackList>
</playlist>
