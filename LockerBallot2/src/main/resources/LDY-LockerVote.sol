pragma solidity ^0.4.25;


/// @title Multisignature wallet - Allows multiple parties to agree on transactions before execution.
/// @author Stefan George - <stefan.george@consensys.net>
contract LockerBallot {

    /*
     *  Constants
     */
    uint8 constant public MAX_OWNER_COUNT = 9;

    mapping (uint => Transaction) public transactions;

    struct Transaction{
        bool initial;
        uint8 adminTransfer;
        address creator;
        address admin;
        mapping (address => uint8) confirmations;
        mapping (address => bool) isOwner;
        address[] owners;
        uint required;
    }

    /*
     *  过滤器
     */

    modifier transactionExists(uint transactionId) {
        require(transactions[transactionId].initial);
        _;
    }

    modifier transactionNotExists(uint transactionId) {
        require(!transactions[transactionId].initial);
        _;
    }

    // 确认 该地址 是投票人
    modifier ownerExists(uint transactionId, address owner) {
        Transaction storage _tran = transactions[transactionId];
        //require(_tran);
        require(_tran.isOwner[owner]);
        _;
    }

    // 确认 该地址 已投票（赞成1、反对2）
    modifier confirmed(uint transactionId, address owner) {
        Transaction storage _tran = transactions[transactionId];
        //require(_tran);
        require(_tran.confirmations[owner] != 0);
        _;
    }

    // 确认 该地址 暂时未投票（暂未投票0）
    modifier notConfirmed(uint transactionId, address owner) {
        Transaction storage _tran = transactions[transactionId];
        //require(_tran);
        require(_tran.confirmations[owner] == 0);
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

    // 确认是管理员
    modifier isAdmin(uint transactionId, address admin){
        require(transactions[transactionId].admin == admin);
        _;
    }

    modifier notTransfer(uint transactionId){
        require(transactions[transactionId].adminTransfer == 0);
        _;
    }

    /*
     * Public functions
     */
    constructor()
    public
    {}

    function addTransaction(uint transactionId, address admin, address[] owners, uint required)
    public
    transactionNotExists(transactionId)
    notNull(admin)
    validRequirement(owners.length, required){
        transactions[transactionId] = Transaction(true, 0, admin, msg.sender, owners, required);
        for(uint i = 0; i < owners.length; i++){
            if(owners[i] == msg.sender){
                transactions[transactionId].confirmations[owners[i]] = 1;
            }else{
                transactions[transactionId].confirmations[owners[i]] = 0;
            }
            transactions[transactionId].isOwner[owners[i]] = true;
        }

    }


    function getRequired(uint transactionId)
    view
    public
    transactionExists(transactionId)
    returns (uint _required)
    {
        Transaction memory tran = transactions[transactionId];
        _required = tran.required;
    }

    /// @dev 审批员投赞成票.
    function confirmTransaction(uint transactionId)
    public
    transactionExists(transactionId)
    ownerExists(transactionId, msg.sender)
    notConfirmed(transactionId, msg.sender)
    {
        Transaction storage tran = transactions[transactionId];
        tran.confirmations[msg.sender] = 1;
    }

    /// @dev 审批员投反对票.
    function refuseConfirmation(uint transactionId)
    public
    transactionExists(transactionId)
    ownerExists(transactionId, msg.sender)
    notConfirmed(transactionId, msg.sender)
    {
        Transaction storage tran = transactions[transactionId];
        tran.confirmations[msg.sender] = 2;
    }

    /// @dev 管理员同意转帐.
    function adminAgreeTransfer(uint transactionId)
    public
    transactionExists(transactionId)
    isAdmin(transactionId, msg.sender)
    notTransfer(transactionId)
    {
        Transaction storage tran = transactions[transactionId];
        tran.adminTransfer = 1;
    }

    /// @dev 管理员拒绝转账.
    function adminRefuseTransfer(uint transactionId)
    public
    transactionExists(transactionId)
    isAdmin(transactionId, msg.sender)
    notTransfer(transactionId)
    {
        Transaction storage tran = transactions[transactionId];
        tran.adminTransfer= 2;
    }


    /// 查询审批员是否已投票 （传入地址必须是必须是审批员）
    function isVote(uint transactionId, address owner)
    view
    public
    notNull(owner)
    transactionExists(transactionId)
    ownerExists(transactionId, owner)
    returns (bool voteFlag)
    {
        Transaction storage tran = transactions[transactionId];
        if(tran.confirmations[owner] != 0)
        {
            voteFlag = true;
        }
        else
        {
            voteFlag = false;
        }
    }

    /// 查询投票状态（传入地址必须是必须是审批员）
    function getVoteValue(uint transactionId, address owner)
    view
    public
    notNull(owner)
    transactionExists(transactionId)
    ownerExists(transactionId, owner)
    returns (uint voteValue)
    {
        Transaction storage tran = transactions[transactionId];
        voteValue = tran.confirmations[owner];
    }

    /// @dev 判断交易是否被确认（审批通过数 >= required设定值）
    function isApprovalConfirmed(uint transactionId)
    public
    transactionExists(transactionId)
    constant
    returns (bool)
    {
        Transaction storage tran = transactions[transactionId];
        uint count = 0;
        for (uint i = 0; i < tran.owners.length; i++) {
            if (tran.confirmations[tran.owners[i]] == 1)
                count += 1;
            if (count >= tran.required)
                return true;
        }
    }

    /// @dev 判断交易是否被拒绝（审批员总数 - 审批拒绝数 < required设定值)
    function isApprovalRefused(uint transactionId)
    public
    transactionExists(transactionId)
    constant
    returns (bool)
    {
        Transaction storage tran = transactions[transactionId];
        uint count = 0;
        for (uint i = 0; i < tran.owners.length; i++) {
            if (tran.confirmations[tran.owners[i]] == 2)
                count += 1;
            if (tran.owners.length - count < tran.required)
                return true;
        }
    }

    /// @dev 判断管理员是否同意转账
    function isAdminAgreed(uint transactionId)
    public
    transactionExists(transactionId)
    constant
    returns (bool)
    {
        if(transactions[transactionId].adminTransfer == 1){
            return true;
        }else{
            return false;
        }
    }

    /// @dev 判断管理员是否拒绝转账
    function isAdminRefused(uint transactionId)
    public
    transactionExists(transactionId)
    constant
    returns (bool)
    {
        if(transactions[transactionId].adminTransfer == 2){
            return true;
        }else{
            return false;
        }
    }

    /*
     * Web3 call functions
     */
    /// @dev 获取 审批赞成票数.
    function getConfirmationCount(uint transactionId)
    public
    transactionExists(transactionId)
    view
    returns (uint count)
    {
        Transaction storage tran = transactions[transactionId];
        for (uint i = 0; i < tran.owners.length; i++)
            if (tran.confirmations[tran.owners[i]] == 1)
                count += 1;
    }

    /// @dev 获取所有审批员 地址.
    /// @return List of owner addresses.
    function getOwners(uint transactionId)
    public
    transactionExists(transactionId)
    view
    returns (address[])
    {
        Transaction storage tran = transactions[transactionId];
        return tran.owners;
    }

    /// @dev 获取 已投赞成票的所有审批员的address（数组）
    function getConfirmOwners(uint transactionId)
    public
    transactionExists(transactionId)
    view
    returns (address[] _confirmations)
    {
        Transaction storage tran = transactions[transactionId];
        address[] memory confirmationsTemp = new address[](tran.owners.length);
        uint count = 0;
        uint i;
        for (i = 0; i < tran.owners.length; i++)
            if (tran.confirmations[tran.owners[i]] == 1) {
                confirmationsTemp[count] = tran.owners[i];
                count += 1;
            }
        _confirmations = new address[](count);
        for (i = 0; i < count; i++)
            _confirmations[i] = confirmationsTemp[i];
    }

    /// @dev 获取 已投反对票的所有审批员的address（数组）
    function getRefuseOwners(uint transactionId)
    public
    transactionExists(transactionId)
    view
    returns (address[] _confirmations)
    {
        Transaction storage tran = transactions[transactionId];
        address[] memory confirmationsTemp = new address[](tran.owners.length);
        uint count = 0;
        uint i;
        for (i=0; i<tran.owners.length; i++)
            if (tran.confirmations[tran.owners[i]] == 2) {
                confirmationsTemp[count] = tran.owners[i];
                count += 1;
            }
        _confirmations = new address[](count);
        for (i=0; i<count; i++)
            _confirmations[i] = confirmationsTemp[i];
    }

    /// @dev 获取 暂未投票 的所有审批员的address（数组）
    function getWaitDoOwners(uint transactionId)
    public
    transactionExists(transactionId)
    view
    returns (address[] _confirmations)
    {
        Transaction storage tran = transactions[transactionId];
        address[] memory confirmationsTemp = new address[](tran.owners.length);
        uint count = 0;
        uint i;
        for (i=0; i<tran.owners.length; i++)
            if (tran.confirmations[tran.owners[i]] == 0) {
                confirmationsTemp[count] = tran.owners[i];
                count += 1;
            }
        _confirmations = new address[](count);
        for (i=0; i<count; i++)
            _confirmations[i] = confirmationsTemp[i];
    }

}
