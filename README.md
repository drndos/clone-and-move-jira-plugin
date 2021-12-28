# clone-and-move-jira-plugin
Fork of the original clone-and-move-jira-plugin by Netapsys Conseil.

Version 2.10.0:
 - Added compatibility with Jira 8.21.0
 - Added slovak translation
 - Refactoring

Version 2.9.0:
 - Added compatiblity with Jira 8.1.0

## How to build
You will need either atlassian SDK or download jndi and jta to your local .m2 repository. Then run:
`clean install`
For testing purposes
`clean jira:run -Dproduct.version=8.21.0`
