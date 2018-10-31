pragma solidity ^0.4.15;


/// @title Multisignature wallet - Allows multiple parties to agree on transactions before execution.
/// @author Stefan George - <stefan.george@consensys.net>
contract LockerBallot {

    /*
     *  Constants
     */
    uint constant public MAX_OWNER_COUNT = 9;

    /*
     *  Storage  备注：chairperson:合约部署者的地址，默认将此地址作为 locker 的CEO（管理员）
     *                 owners：审批员 地址 集合
     *                 confirmations:  key: 投票人的address  =>  value: 投票结果（0待投票，1赞成，2拒绝） }
     */
    mapping (address => uint) public confirmations;
    mapping (address => bool) public isOwner;
    address[] public owners;
    uint public required;

    /*
     *  过滤器
     */

    // 确认 该地址 是投票人
    modifier ownerExists(address owner) {
        require(isOwner[owner]);
        _;
    }

    // 确认 该地址 已投票（赞成1、反对2）
    modifier confirmed(address owner) {
        require(confirmations[owner] != 0);
        _;
    }

    // 确认 该地址 暂时未投票（暂未投票0）
    modifier notConfirmed(address owner) {
        require(confirmations[owner] == 0);
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
    constructor(address[] _owners, uint _required)
        public
        validRequirement(_owners.length, _required)
    {
        for (uint i=0; i<_owners.length; i++) {
            require(!isOwner[_owners[i]] && _owners[i] != 0);
            isOwner[_owners[i]] = true;
            if(msg.sender == _owners[i])
            {
                confirmations[_owners[i]] = 1;
            }
            else
            {
                confirmations[_owners[i]] = 0;
            }
        }
        owners = _owners;
        required = _required;
    }

    function getRequired()
        view
        public
        returns (uint _required)
    {
        _required = required;
    }

    /// @dev 审批员投赞成票.
    function confirmTransaction()
        public
        ownerExists(msg.sender)
        notConfirmed(msg.sender)
    {
        confirmations[msg.sender] = 1;
    }

    /// @dev 审批员投反对票.
    function refuseConfirmation()
        public
        ownerExists(msg.sender)
        notConfirmed(msg.sender)
    {
        confirmations[msg.sender] = 2;
    }

    /// 查询是否已投票 （传入地址必须是必须是审批员）
    function isVote(address owner)
        view
        public
        ownerExists(owner)
        returns (bool voteFlag)
    {
        if(confirmations[owner] != 0)
        {
            voteFlag = true;
        }
        else
        {
            voteFlag = false;
        }
    }

     /// 查询投票状态（传入地址必须是必须是审批员）
    function getVoteValue(address owner)
        view
        public
        ownerExists(owner)
        returns (uint voteValue)
    {
        voteValue = confirmations[owner];
    }

    /// @dev 判断交易是否被确认（审批通过数 >= required设定值）
    function isConfirmed()
        public
        constant
        returns (bool)
    {
        uint count = 0;
        for (uint i=0; i<owners.length; i++) {
            if (confirmations[owners[i]] == 1)
                count += 1;
            if (count >= required)
                return true;
        }
    }

    /// @dev 判断交易是否被拒绝（审批员总数 - 审批拒绝数 < required设定值)
    function isRefused()
        public
        constant
        returns (bool)
    {
        uint count = 0;
        for (uint i=0; i<owners.length; i++) {
            if (confirmations[owners[i]] == 2)
                count += 1;
            if (owners.length - count < required)
                return true;
        }
    }

    /*
     * Web3 call functions
     */
    /// @dev 获取 审批赞成票数.
    function getConfirmationCount()
        public
        constant
        returns (uint count)
    {
        for (uint i=0; i<owners.length; i++)
            if (confirmations[owners[i]] == 1)
                count += 1;
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

    /// @dev 获取 已投赞成票的所有审批员的address（数组）
    function getConfirmOwners()
        public
        constant
        returns (address[] _confirmations)
    {
        address[] memory confirmationsTemp = new address[](owners.length);
        uint count = 0;
        uint i;
        for (i=0; i<owners.length; i++)
            if (confirmations[owners[i]] == 1) {
                confirmationsTemp[count] = owners[i];
                count += 1;
            }
        _confirmations = new address[](count);
        for (i=0; i<count; i++)
            _confirmations[i] = confirmationsTemp[i];
    }

    /// @dev 获取 已投反对票的所有审批员的address（数组）
    function getRefuseOwners()
        public
        constant
        returns (address[] _confirmations)
    {
        address[] memory confirmationsTemp = new address[](owners.length);
        uint count = 0;
        uint i;
        for (i=0; i<owners.length; i++)
            if (confirmations[owners[i]] == 2) {
                confirmationsTemp[count] = owners[i];
                count += 1;
            }
        _confirmations = new address[](count);
        for (i=0; i<count; i++)
            _confirmations[i] = confirmationsTemp[i];
    }

    /// @dev 获取 暂未投票 的所有审批员的address（数组）
    function getWaitDoOwners()
        public
        constant
        returns (address[] _confirmations)
    {
        address[] memory confirmationsTemp = new address[](owners.length);
        uint count = 0;
        uint i;
        for (i=0; i<owners.length; i++)
            if (confirmations[owners[i]] == 0) {
                confirmationsTemp[count] = owners[i];
                count += 1;
            }
        _confirmations = new address[](count);
        for (i=0; i<count; i++)
            _confirmations[i] = confirmationsTemp[i];
    }

}
