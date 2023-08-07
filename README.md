# [Composum](https://www.composum.com/home.html)

An [Apache Sling](https://sling.apache.org/) based Application Platform

## [Composum Platform Tenants](https://www.composum.com/home/platform/extensions/tenants.html)

The Composum Platform multi tenants extension allows managing multiple sites for various customers, corporate divisions,
or subsidiaries on one Apache Sling platform. Each tenant has its own applications, components, and designs on the
platform, making it easy to handle multiple internet sites. The extension includes a Tenants Console within the Composum
Nodes Console, where administrators can add, configure, and remove tenants. Predefined roles are set up for each tenant,
and repository access control lists (ACLs) based on these roles ensure separation between tenants.

User management is also a part of this extension, allowing registered users to be assigned to tenant roles. The Tenants
Management Console displays all users joined to tenants and their respective roles. Tenant managers, with the 'manager'
role, have the ability to add and remove users, as well as modify their roles within the tenant.

The extension further offers host management, where a tenant manager can add, remove, and configure internet hosts for
their domains. Host declarations are only useful if the DNS configuration connects the host to the Composum Platform
system. Tenant managers can configure webserver rules, map hosts to the platform, and assign hosts to a specific stage
of a site ('preview' or 'public'). SSL certificates can also be requested and configured for secure hosting.

Additionally, the Tenants extension includes workflow processing for defining and using collaborative processes. The
Workflow Inbox view in the Tenants Console enables users to start and process workflows. This workflow option is also
integrated into the Composum Pages user interface, allowing users without access to the Tenants Console to work with
workflows within the Pages editing frame.

Overall, the Composum Platform Tenants extension provides comprehensive tenant management capabilities, including user
management, host management, and workflow processing, allowing for efficient management of multiple sites on a single
platform.

Detailed documentation is available [here](https://www.composum.com/home/platform/extensions/tenants.html).

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

### See also

* [Composum Homepage](https://www.composum.com/home/pages.html)
* [Composum Nodes](https://github.com/ist-dresden/composum)
* [Composum Pages](https://www.composum.com/home/pages.html)
* [Composum Assets](https://github.com/ist-dresden/composum-assets)
* [Composum Platform](https://www.composum.com/home/platform.html)
