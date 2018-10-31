pragma solidity ^0.4.15;


/// @title Multisignature wallet - Allows multiple parties to agree on transactions before execution.
/// @author Stefan George - <stefan.george@consensys.net>
contract MultiSigWallet {

    /*
     *  Events
     */
    event Confirmation(address indexed sender, uint indexed transactionId);
    event Revocation(address indexed sender, uint indexed transactionId);
    event Submission(uint indexed transactionId);
    event Execution(uint indexed transactionId);
    event ExecutionFailure(uint indexed transactionId);
    event OwnerAddition(address indexed owner);
    event StaffAddition(address indexed staff);
    event OwnerRemoval(address indexed owner);
    event StaffRemoval(address indexed staff);
    event RequirementChange(uint required);

    /*
     *  Constants
     */
    uint constant public MAX_OWNER_COUNT = 9;

    /*
     *  Storage  备注：chairperson:合约部署者的地址，默认将此地址作为 locker 的CEO（管理员）
     *                 owners：审批员 地址 集合
     *                 staffs：员工 地址 集合（包含审批员）
     *                 confirmations: { key: transactionId  =>  value: mapping{ key: 投票人的address  =>  value: 投票结果（0待投票，1赞成，2拒绝）} }
     */
    mapping (uint => Transaction) public transactions;
    mapping (uint => mapping (address => uint)) public confirmations;
    mapping (uint => mapping (address => bool)) public isTransactionOwners;
    mapping (address => bool) public isOwner;
    mapping (address => bool) public isStaff;
    address[] public owners;
    address[] public staffs;
    uint public required;
    uint public transactionCount;
    address public chairperson;

    /*结构体：用于记录一笔交易的信息*/
    // initiator 审批发起人地址
    struct Transaction {
        address initiator;
        address[] owners;
        uint required;
    }

    /*
     *  过滤器
     */
    // 确认调用合约的address是 locker的CEO（管理员）
    modifier onlyWallet() {
        require(msg.sender == chairperson);
        _;
    }

    // 确认 该地址 暂时还不是投票人
    modifier ownerDoesNotExist(address owner) {
        require(!isOwner[owner]);
        _;
    }

    // 确认 该地址 暂时还不是locker员工
    modifier staffDoesNotExist(address staff) {
        require(!isStaff[staff]);
        _;
    }

    // 确认 该地址 是投票人
    modifier ownerExists(address owner) {
        require(isOwner[owner]);
        _;
    }


    // 确认 该地址 是 transactionId 投票人
    modifier ownerExistsInTransaction(uint transactionId, address owner) {
        require(isTransactionOwners[transactionId][owner]);
        _;
    }

    // 确认 该地址 是locker员工
    modifier staffExists(address staff) {
        require(isStaff[staff]);
        _;
    }

    // 确认 存在该交易审批的id
    modifier transactionExists(uint transactionId) {
        require(transactions[transactionId].initiator != 0);
        _;
    }

    // 确认 不存在该交易审批的id
    modifier transactionDoesNotExists(uint transactionId) {
        require(transactions[transactionId].initiator == 0);
        _;
    }

    // 确认 该地址 已投票（赞成1、反对2）
    modifier confirmed(uint transactionId, address owner) {
        require(confirmations[transactionId][owner] != 0);
        _;
    }

    // 确认 该地址 暂时未投票（暂未投票0）
    modifier notConfirmed(uint transactionId, address owner) {
        require(confirmations[transactionId][owner] == 0);
        _;
    }

    // 确认该地址 是否有效
    modifier notNull(address _address) {
        require(_address != 0);
        _;
    }

    // 确认 _required 和 审批员数量的合法性
    modifier validRequirement(uint ownerCount, uint _required) {
        require(ownerCount <= MAX_OWNER_COUNT
            && _required <= ownerCount
            && _required != 0
            && ownerCount != 0);
        _;
    }

    /*
     * Public functions
     */
    /// @dev Contract constructor sets initial owners and required number of confirmations.
    /// @param _owners List of initial owners.
    /// @param _required Number of required confirmations.
    constructor(address[] _staffs, address[] _owners, uint _required)
        public
        validRequirement(_owners.length, _required)
    {
        chairperson = msg.sender;
        staffs = _staffs;
        for (uint i=0; i<_staffs.length; i++) {
            require(!isStaff[_staffs[i]] && _staffs[i] != 0);
            isStaff[_owners[i]] = true;
        }
        for (i=0; i<_owners.length; i++) {
            require(!isOwner[_owners[i]] && _owners[i] != 0);
            if(!isStaff[_owners[i]])
            {
                isStaff[_owners[i]] = true;
                staffs.push(_owners[i]);
            }
            isOwner[_owners[i]] = true;
        }
        if(!isStaff[chairperson])
        {
            isStaff[chairperson] = true;
            staffs.push(chairperson);
        }

        owners = _owners;
        required = _required;
    }

    /// @dev Allows to add a new owner. Transaction has to be sent by wallet.
    /// @param owner Address of new owner.
    function addOwner(address owner)
        public
        onlyWallet
        ownerDoesNotExist(owner)
        notNull(owner)
        validRequirement(owners.length + 1, required)
    {
        if(!isStaff[owner]){
            isStaff[owner] = true;
            staffs.push(owner);
            emit StaffAddition(owner);
        }
        isOwner[owner] = true;
        owners.push(owner);
        emit OwnerAddition(owner);
    }

    function addStaff(address staff)
        public
        onlyWallet
        staffDoesNotExist(staff)
        notNull(staff)
    {
        isStaff[staff] = true;
        staffs.push(staff);
        emit StaffAddition(staff);
    }

    /// @dev 删除 审批员.只有CEO（管理员）有权限
    /// @param owner Address of owner.
    function removeOwner(address owner)
        public
        onlyWallet
        notNull(owner)
        ownerExists(owner)
    {
        isOwner[owner] = false;
        for (uint i=0; i<owners.length - 1; i++)
            if (owners[i] == owner) {
                owners[i] = owners[owners.length - 1];
                break;
            }
        owners.length -= 1;
        if (required > owners.length)
            changeRequirement(owners.length);
        emit OwnerRemoval(owner);
    }

    /// @dev 删除 员工。只有CEO（管理员）有权限，并且该 员工 不是 审批员
    function removeStaff(address staff)
        public
        onlyWallet
        notNull(staff)
        ownerDoesNotExist(staff)
        staffExists(staff)
    {
        isStaff[staff] = false;
        for (uint i=0; i<staffs.length - 1; i++)
            if (staffs[i] == staff) {
                staffs[i] = staffs[staffs.length - 1];
                break;
            }
        staffs.length -= 1;
        emit StaffRemoval(staff);
    }

     /// @dev 删除 员工,如果员工是审批员，连带审批员一起删除。只有CEO（管理员）有权限
    function deleteStaff(address staff)
        public
        onlyWallet
        notNull(staff)
        staffExists(staff)
    {
        if(isOwner[staff])
        {
            isOwner[staff] = false;
            for (uint i=0; i<owners.length - 1; i++)
                if (owners[i] == staff) {
                    owners[i] = owners[owners.length - 1];
                    break;
                }
            owners.length -= 1;
            if (required > owners.length)
                changeRequirement(owners.length);
            emit OwnerRemoval(staff);
        }
        isStaff[staff] = false;
        for (i=0; i<staffs.length - 1; i++)
            if (staffs[i] == staff) {
                staffs[i] = staffs[staffs.length - 1];
                break;
            }
        staffs.length -= 1;
        emit StaffRemoval(staff);
    }

    /// @dev 替换审批员，新审批员必须是一个员工
    /// @param owner Address of owner to be replaced.
    /// @param newOwner Address of new owner.
    function replaceOwner(address owner, address newOwner)
        public
        onlyWallet
        ownerExists(owner)
        staffExists(newOwner)
        ownerDoesNotExist(newOwner)
    {
        for (uint i=0; i<owners.length; i++)
            if (owners[i] == owner) {
                owners[i] = newOwner;
                break;
            }
        isOwner[owner] = false;
        isOwner[newOwner] = true;
        emit OwnerRemoval(owner);
        emit OwnerAddition(newOwner);
    }

    /// 替换员工，新员工 必须 不是员工
    function replaceStaff(address staff, address newStaff)
        public
        onlyWallet
        staffExists(staff)
        ownerDoesNotExist(staff)
        staffDoesNotExist(newStaff)
    {
        for (uint i=0; i<staffs.length; i++)
            if (staffs[i] == staff) {
                staffs[i] = newStaff;
                break;
            }
        isStaff[staff] = false;
        isStaff[newStaff] = true;
        emit StaffRemoval(staff);
        emit StaffAddition(newStaff);
    }

    function getRequirement()
        view
        public
        returns (uint _required)
    {
        _required = required;
    }


    function getRequirementByTransactionId(uint transactionId)
        view
        public
        returns (uint _required)
    {
        _required = transactions[transactionId].required;
    }

    /// @dev Allows to change the number of required confirmations. Transaction has to be sent by wallet.
    /// @param _required Number of required confirmations.
    function changeRequirement(uint _required)
        public
        onlyWallet
        validRequirement(owners.length, _required)
    {
        required = _required;
        emit RequirementChange(_required);
    }

    /// @dev 审批员投赞成票.
    /// @param transactionId Transaction ID.
    function confirmTransaction(uint transactionId)
        public
        transactionExists(transactionId)
        ownerExistsInTransaction(transactionId,msg.sender)
        notConfirmed(transactionId, msg.sender)
    {
        confirmations[transactionId][msg.sender] = 1;
        emit Confirmation(msg.sender, transactionId);
    }

    /// @dev 审批员投反对票.
    /// @param transactionId Transaction ID.
    function refuseConfirmation(uint transactionId)
        public
        transactionExists(transactionId)
        ownerExistsInTransaction(transactionId,msg.sender)
        notConfirmed(transactionId, msg.sender)
    {
        confirmations[transactionId][msg.sender] = 2;
        emit Revocation(msg.sender, transactionId);
    }

    /// @dev 判断是否 审批通过数 >= required设定值
    /// @param transactionId Transaction ID.
    /// @return Confirmation status.
    function isConfirmed(uint transactionId)
        public
        constant
        returns (bool)
    {
        uint count = 0;
        address[] storage _owners = transactions[transactionId].owners;
        uint _required = transactions[transactionId].required;
        for (uint i=0; i<_owners.length; i++) {
            if (confirmations[transactionId][_owners[i]] == 1)
                count += 1;
            if (count >= _required)
                return true;
        }
    }

    /*
     * Internal functions
     */
    /// @dev locker的员工发起审批流程，用此方法,如果员工是审批员，直接投赞成票
    /// @param transactionId Transaction transactionId.
    /// @return Returns transaction ID.
    function addTransaction(uint transactionId)
        public
        staffExists(msg.sender)
        transactionDoesNotExists(transactionId)
        returns (uint _transactionId)
    {
        address[] storage _owners = owners;
        _transactionId = transactionId;
        transactions[transactionId] = Transaction({
            initiator: msg.sender,
            owners: _owners,
            required: required

        });
        for (uint i=0; i<_owners.length; i++) {
            isTransactionOwners[transactionId][_owners[i]] = true;
        }
        transactionCount += 1;

        if(isTransactionOwners[transactionId][msg.sender]){
            confirmations[transactionId][msg.sender] = 1;
        }
        emit Submission(_transactionId);
    }

    /*
     * Web3 call functions
     */
    /// @dev 获取transaction审批赞成票数.
    /// @param transactionId Transaction ID.
    /// @return Number of confirmations.
    function getConfirmationCount(uint transactionId)
        public
        constant
        returns (uint count)
    {
        address[] storage _owners = transactions[transactionId].owners;
        for (uint i=0; i<_owners.length; i++)
            if (confirmations[transactionId][_owners[i]] == 1)
                count += 1;
    }

    /// @dev 获取 已发起的审批 数量。
    /// @return Total number of transactions after filters are applied.
    function getTransactionCount()
        public
        constant
        returns (uint count)
    {
        count = transactionCount;
    }

    /// @dev 获取所有审批员 地址.
    /// @return List of owner addresses.
    function getOwners()
        public
        constant
        returns (address[])
    {
        return owners;
    }

    /// @dev 获取transactionId的所有审批员 地址.
    /// @return List of owner addresses.
    function getTransactionOwners(uint transactionId)
        public
        constant
        returns (address[])
    {
        return transactions[transactionId].owners;
    }

    /// @dev 获取所有员工 地址.
    function getStaffs()
        public
        constant
        returns (address[])
    {
        return staffs;
    }

    /// @dev 获取对transactionId已投赞成票的所有审批员的address（数组）
    /// @param transactionId Transaction ID.
    /// @return Returns array of owner addresses.
    function getConfirmations(uint transactionId)
        public
        constant
        returns (address[] _confirmations)
    {
        address[] storage _owners = transactions[transactionId].owners;
        address[] memory confirmationsTemp = new address[](_owners.length);
        uint count = 0;
        uint i;
        for (i=0; i<_owners.length; i++)
            if (confirmations[transactionId][_owners[i]] == 1) {
                confirmationsTemp[count] = _owners[i];
                count += 1;
            }
        _confirmations = new address[](count);
        for (i=0; i<count; i++)
            _confirmations[i] = confirmationsTemp[i];
    }

}
