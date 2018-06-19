Account API demo in Java + Spark with JUnit tests.

- run with ./rundev.sh
- test with mvn test

# Temporary notes

- using namespaces based on features not layer (to avoid splitting e.g. interface, model and DAO across multiple folders)
- thought process
    - accounts can store different types of values - money, cryptocurrencies, and securities (keeping to these 3)
    - transfer must be between compatible types of accounts
    - accounts can be owned by individuals, but also companies
    - accounts may require multi-user approval (e.g. there could be escrow requirements, or a company account can be controlled by some board members jointly, but not individually)
        - the authorization requirements stem from the individuals, not the accounts (e.g. person A can control an account on their own, but person B and C need to do it jointly)
    - some users can initiate a transfer request (but not initiate the actual transfer). E.g. a user specifies "I need to pay X", but a different user must approve
    - some transactions need more information to be approved (approval by user X, or multiple users, or even paperwork, or even manual admin approval/account on hold), and then they can be executed
    - some transactions will be denied after providing more information (e.g. need KYC for user X, and KYC indicates they are in a sanction list)
    - some transactions will always have priority, and some transactions will be automatic (provided there is sufficient balance)
        - e.g. automatic mortgage payment to the bank's account (which can leave the account in the red)
    - some transactions will be timed
    - some transactions will allow overdraft, some will not
        - some of this determined by the account type (credit account) and some determined by special privileges (e.g. Nordea allows an overdraft transaction from a regular account, if they are the recipient)
    - KYC/sanction lists (abstract away as an opaque thing)
    - security
        - 1 security check is "is this transaction approved by the people who have the legal authority"
        - another is - can this transaction go through at all? (sufficient balance/involved accounts on hold/missing KYC/sanction list information for the recipients)
    - when is a transfer marked completed?
        - wire/SEPA/cryptocurrencies? Not sure how it goes re: SEPA/wire/stock transfers, but different cryptocurrencies need different confirmation counts
        - stub out a transaction approval service, which marks "in progress" transactions as completed?
    - there needs to be some notification capability (to know when e.g. final approval needs to be done for a transaction) associated with this
    - legal approval for transactions comes from just 1 place (the owner of the account, which is an entity not a user)
        - legal approval for an entity is composed by multiple users
    - who can own accounts?
        - at a granular level, individuals, for-profits, governments, charities, etc.
        - for the purposes of approval, this is not important. Need to map "rules" on how users form a definitive legal approval.
        - individuals
        - legal entities
    - ASSUMPTION: approval will always come down to a "person" approving or denying something. Should systems approve something?
    - ASSUMPTION: though end-user authentication is not involved, it could be useful to have "forced" transactions require a signature from an HSM (to avoid tampering by employees, even for an internal system)
    - SKIPPED: for an internal system, need to get feedback about "what's next" so the frontend can gather that data etc.
        - out of scope. This API only reacts to events, so whichever system interacts with it, can also generate the notifications
    - detailed auditing is important, so allow for that
        - just a dummy interface, as it is out-of-scope, and detailed requirements would probably come from the legal department
- conclusions:
    - same user or other users can submit information required to approve the transaction
    - accounts have two balances (actual, and available)
    - transactions need to have ways to be flagged (maybe not on the transaction object, but invoke another API which decides who and when to notify)
    - how to model approval
        - not via Users/LegalEntities/etc. (irrelevant and constricting at this level), but with Approvers
            - each approver has a Guid (can map to tax residency numbers, company IDs, private keys)
            - system accepts a blank "Approver X says Yes/No"
            - all will be verified, despite being an internal system:
                - i.e. frontend claims user X pressed Approve, but they still have to enter a 2FA code
                - OPTIONAL: automatic acceptance if no 2FA enabled, OR for small sums (e.g. until a threshold)
                - i.e. cryptocurrency transfer could require a verifiable signature, which allows keeping the keys with the user (and even could require response to a challenge, in case of non-custodial multisig/interaction with smart contracts)
                - a "forced" transaction needs to be signed by an HSM to minimize risk of employee negligence/mistakes/malicious use (for auditors)
                - approval can take the form of a digitally signed document (so a user can e-mail it in). The system can verify it contains "I approve transaction X" and is signed in an eIDAS compliant manner
        - implementation ideas:
            - system generates "approval requests" and waits for them to be submitted by the outside system
            - approval requests need to model AND and OR relationships. Hierarchy could be:
                - TransferRequest
                |-- TransferApprovalRequestGroup (type AND) (one root group for each request, parent NULL)
                  |-- TransferApprovalRequest  (refers to the group) (can contain a challenge etc.)
                  |-- TransferApprovalRequestGroup (type AND/OR) (parent group is NOT NULL)
            - this allows modelling multisig, multi factor authorization etc.
            - distinction between group/request can further be removed, by having requests point to an optional parent-request
    - proposed transaction lifecycle:
        - a user or a system creates a transaction request:
            - check if they have permission to create the request
            - check if there is sufficient (confirmed) balance in the account
                - if insufficient balance, check max overdraft. Allow to override (since this is an internal system API) to force the payment regardless
        - check the transaction legal approval workflow:
            - always ends with some [users] (not entities) who can approve the transaction (in case of a multisig account, require approval of multiple, but eventually it will be just one)
            - notify the users when they are added to a transaction request as approvers (or when the transaction is prematurely denied by somebody)
            - can have multiple steps (e.g. before a user can start approving, they may be required to submit KYC information, and a sanctions checks might be run, which determines final eligibility to participate. If ineligible, transaction not cancelled, but flagged)
        - when approved, transaction goes into "pending" state and the "available" value of the account changes
            - marked as "finalized" by an external service


        -

