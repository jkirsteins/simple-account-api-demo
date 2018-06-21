# Account API demo in Java + Spark with JUnit tests.

This is an exploration of what a backend REST API for a financial services institution might look like
initially. It is written as a learning exercise, using Java and [Spark](http://sparkjava.com). 

- on Linux or macOS you can easily run it using Spark's embedded server with `./rundev.sh` 
- execute all tests with `mvn test`

## Thoughts on the Design

### Account Types

Most financial services support (or aspire to support) multiple types of assets (e.g. money, cryptocurrencies, securities).
  
In this thought experiment, let's assume they are all equivalent.

### Transfer Constraints

- Transfers can happen between more than 2 accounts (e.g. account A can send to B, or it could sent to B and C simultaneously. B could
  be the main recipient, and C could be e.g. an account for collecting transfer fees)
- Transfers must happen between accounts of the same denomination (e.g. no ETH <-> EUR transfers). Although, in the future,
  a conversion layer could be introduced (e.g. send Ethereum to a Euro account using a specific conversion rate. Alternatively, send
  Euro and collect a fee in Ethereum).
- Support for overdrafts should be allowed.

### Account Types, Owner/User Entities, KYC ...

All of this can be kept on the frontend. If - in the future - it turns out that more integration of these concerns
is required, then the ApprovalRequirement system can be updated to include the new requirements (see *Transfer Approvals*).

### Transfer Approvals

If we assume transfers are created directly in one call, then we offload complexity to the API consumer. It is likely that a 
typical transfer backend API would be called by multiple different subsystems, so it could lead to a divergent way in
how transfers are approved.

Furthermore, advanced use cases (such as a non-custodial cryptocurrency service), multi-factor authentication, or
an regulatory requirement to keep cryptographically signed audits, might stipulate approval using external information (i.e. 
we can not say "create this transfer because we trust the API caller". Instead "even though we trust the API caller, we need more information"). 
This extra information could be a pre-prepared signed cryptocurrency transaction, a one-time factor code from a hardware token,
or a signed approval (where the signing key is kept in a hardware security module).

### Transfer Finalization

After a transfer is approved and initiated, it still needs to be finalized. This can mean:

- a cryptocurrency transaction has achieved sufficient confirmation count, or
- a wire transfer has cleared,
- etc.

To account for this - and the fact that different accounts will need different confirmations - we leave finalization
up to the API consumer.

E.g. a separate service could run cryptocurrency monitoring, and invoke ther API when crypto transfers should be finalized

And a separate service could be responsible for finalizing SEPA transfers, etc.

### Proposed Transfer Lifecycle

A simple initial transfer lifecycle could be:

- create a transfer request between accounts, perform validation (sufficient balance, matching account types etc.)
- API consumer requests a list of additional information required (a collection of [ApprovalRequirement] objectsw). 
  
  In theory, the collection can be empty (and the transfer initiated immediately). Or it could ask for more information,
  the gathering of which is up to the frontend/API consumer.
  
  Each ApprovalRequirement is generic - it can have a type, and an optional request, and an optional response field. Based
  on the type, the approval service determines how to interpret these fields.
  
  The approval service could be built-in, or offloaded to an external API.
- a transfer request can specify that overdraft is allowed. We trust the caller.
- a transfer remains in the "pending" state until it is finalized by an external API caller.

### Fees

This concept is deliberately kept out of scope of the backend API. The caller should account for fees, and include
a transfer recipient in its request.

### Account Balances and Transfer Statuses

- when a transfer is initiated, the available balance of the source account changes (but not the totals of either 
  source or receiving accounts, and not the available balance of the receiving account)
- when a transfer is finalized, the total of the source account should be adjusted accordingly. The receiving accounts
  should have their available/total balance increased immediately.
  
## Resulting API

This project aims to have a strict "resource-based" API, instead of an API that models remote procedures. 

**NOTE: the trailing slash is important**

    # Create an account
    POST /api/v1/account/
    
    # Get an account
    GET /api/v1/account/:visual_id
    
    # Create a transfer request
    POST /api/v1/account/:visual_id/transfer_request/
    
    # Get the status (approved/pending/denied) of a transfer request
    GET /api/v1/account/:visual_id/transfer_request/:transfer_request_id/status
    
    # Get all approval requirements for a transfer request
    GET /api/v1/account/:visual_id/transfer_request/:transfer_request_id/approval_requirement/
    
    # Submit information to satisfy a specific approval requirement for a transfer request
    PUT /api/v1/account/:visual_id/transfer_request/:transfer_request_id/approval_requirement/:approval_requirement_id
    
    # Create a transfer (after a transfer request is approved)
    POST /api/v1/account/:visual_id/transfer/
    
    # Get an existing transfer (pending or finalized)
    GET /api/v1/account/:visual_id/transfer/:transfer_id
    
    # Finalize a pending transfer (meant for external transfer monitoring services)
    PUT/api/v1/account/:visual_id/transfer/:transfer_id
    
### Versioning

It is important to consider API versioning from the ground up. All of the endpoints in this example are put under "/api/v1/".

While an argument could be made that putting the version information in the route is suboptimal, it can be changed
more easily than introducing versioning to an unversioned API.

### Plural vs Singular

This project opts for "always singular" - e.g. "/account/" vs "/accounts/".

## Glossary and Terminology

- the source code uses the term "visual id" when referring to account IDs. The "visual id" - given an account of type X, and name Y, is "X:Y".
  E.g. an "EUR" account "LV40HABA..." will have the "visual id" "EUR:LV40HABA...".
  
## Auditing Requirements

Financial institutions might have strict regulatory auditing requirements, so we can not rely on regular logging.

This should be accounted for, but since the actual auditing requirements can be very specific to the business, no auditing
is implemented (apart from noting that it is an important concept).

Different audit logs can be easily injected using dependency injection, when the need arises.

An example use case of a more-advanced-than-regular-development-log could be a cryptographically tamper-proof log, where
each entry is signed with a master key, and inclues the hash of the previous entry.

## Limitations

- no support for transfer cancellations (e.g. DELETE pending)
- no support for transfers between accounts of different types/denominations
- no actual audit logging

## Tests

- API tests spin up the embedded webserver. Even though this is strictly not correct unit testing, it seems reasonable
  in the case of an API (because the backing classes - even if public - are more implementation details, and it is more
  important to test that they have been mapped to routes correctly)
  
## Known Issues and Limitations

- Spark does not support testing very well, and starting/stopping the embedded webserver can not be done reliably.
  To sidestep this issue, I have introduced a deliberate 2 second delay after every test.
  
  This is a known issue: https://github.com/perwendel/spark/issues/705
  
  The solution is already developed, but a new Spark version has not been released with it. When it is, then
  Spark.stop() should be replaced by Spark.awaitStop(), and the delay removed 
- the in-memory data access objects do not require #update() to be called (because the updates are done directly on the
  master version of each object).
  
  This means that tests using them can be unreliable (e.g. if a service forgets to call #update(), this will not be caught
  in tests, but could cause a problem in production).
  
  To counter this, either a proper in-memory data storage service should be used (incl. proper transaction support too),
  or unit tests should be written for the dummy DAOs to make sure they do require #update.
- Spark treats "/account" and "/account/" as different routes. This could be a problem with a public API (where it is
  better to be lenient), but for an internal API it is likely acceptable.
       
