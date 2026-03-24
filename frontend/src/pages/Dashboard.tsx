import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';
import { User, CreditCard, LogOut, Download, Upload, ArrowRightLeft, Plus, X, AlertCircle, CheckCircle2 } from 'lucide-react';

type ModalType = 'NONE' | 'NEW_ACCOUNT' | 'DEPOSIT' | 'WITHDRAW' | 'TRANSFER';

const Dashboard = () => {
  const [profile, setProfile] = useState<any>(null);
  const [accounts, setAccounts] = useState<any[]>([]);
  const navigate = useNavigate();

  // Modal States
  const [activeModal, setActiveModal] = useState<ModalType>('NONE');
  const [selectedAcc, setSelectedAcc] = useState<string>('');
  const [amount, setAmount] = useState<string>('');
  const [targetAcc, setTargetAcc] = useState<string>('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [feedback, setFeedback] = useState<{type: 'error' | 'success', msg: string} | null>(null);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const username = localStorage.getItem('username');
      const profileRes = await api.get(`/users/${username}`).catch(() => ({ data: null }));
      setProfile(profileRes.data);

      if (profileRes.data) {
        const accountsRes = await api.get(`/accounts/user/${username}`).catch(() => ({ data: [] }));
        
        // Fix: Sort accounts by accountNumber consistently, so the layout doesn't shuffle on balance update
        const sortedAccounts = accountsRes.data.sort((a: any, b: any) => 
          a.accountNumber.localeCompare(b.accountNumber)
        );
        
        setAccounts(sortedAccounts);
      }
    } catch (err) {
      console.log('Error fetching data');
    }
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  const handleCreateProfile = async () => {
    const username = localStorage.getItem('username') || '';
    const fullName = prompt('Enter your full name:', username) || username;
    try {
      await api.post('/users', {
        username,
        fullName,
        email: `${username}@bank.com`,
        phone: '1234567890',
        address: '123 Main St',
        city: 'Metropolis',
        state: 'NY',
        zipCode: '10001',
        panNumber: 'ABCDE1234F',
        aadhaarNumber: '123456789012'
      });
      fetchData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Error creating profile');
    }
  };

  const closeModal = () => {
    setActiveModal('NONE');
    setAmount('');
    setTargetAcc('');
    setSelectedAcc('');
    setFeedback(null);
  };

  const openAction = (type: ModalType, accNum: string = '') => {
    setSelectedAcc(accNum);
    setActiveModal(type);
    setAmount('');
    setTargetAcc('');
    setFeedback(null);
  };

  const executeAction = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsProcessing(true);
    setFeedback(null);
    const numAmount = parseFloat(amount);

    try {
      if (activeModal === 'NEW_ACCOUNT') {
        await api.post('/accounts', {
          username: localStorage.getItem('username'),
          accountType: 'SAVINGS',
          initialDeposit: 1500.0,
          branchCode: 'MAIN01',
          ifscCode: 'BANK0000001'
        });
        setFeedback({ type: 'success', msg: 'Account created successfully!' });
      } else if (activeModal === 'DEPOSIT') {
        await api.post('/transactions/deposit', {
          accountNumber: selectedAcc,
          amount: numAmount,
          description: 'Self Deposit'
        });
        setFeedback({ type: 'success', msg: `Deposited $${numAmount.toFixed(2)} successfully!` });
      } else if (activeModal === 'WITHDRAW') {
        await api.post('/transactions/withdraw', {
          accountNumber: selectedAcc,
          amount: numAmount,
          description: 'Self Withdrawal'
        });
        setFeedback({ type: 'success', msg: `Withdrew $${numAmount.toFixed(2)} successfully!` });
      } else if (activeModal === 'TRANSFER') {
        await api.post('/transactions/transfer', {
          fromAccountNumber: selectedAcc,
          toAccountNumber: targetAcc,
          amount: numAmount,
          description: 'Transfer to another account'
        });
        setFeedback({ type: 'success', msg: `Transferred $${numAmount.toFixed(2)} to ${targetAcc}!` });
      }
      
      await fetchData(); // Refresh data immediately
      setTimeout(() => closeModal(), 2000); // Close modal automatically after success
    } catch (err: any) {
      setFeedback({ type: 'error', msg: err.response?.data?.message || 'Transaction failed. Please try again.' });
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col w-full text-gray-800 relative font-sans">
      <nav className="bg-white border-b border-gray-100 px-6 py-4 shadow-sm flex items-center justify-between sticky top-0 z-40">
        <h1 className="text-2xl font-black bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-indigo-600 tracking-tight">SecureBank</h1>
        <button onClick={handleLogout} className="flex flex-row items-center gap-2 text-gray-500 hover:text-red-500 font-medium transition-colors bg-gray-50 px-4 py-2 rounded-full hover:bg-red-50">
          <LogOut size={18} /> <span className="text-sm">Logout</span>
        </button>
      </nav>

      <main className="flex-1 max-w-6xl w-full mx-auto p-4 md:p-8 space-y-8">
        
        {/* User Profile Section */}
        <div className="bg-white rounded-[2rem] shadow-sm p-6 md:p-8 border border-gray-100 flex items-center justify-between">
          <div className="flex items-center gap-5">
            <div className="bg-gradient-to-br from-blue-100 to-indigo-100 p-5 rounded-[1.5rem] text-blue-600 shadow-inner">
              <User size={32} strokeWidth={2.5} />
            </div>
            <div>
              <p className="text-gray-400 text-sm font-medium mb-1 uppercase tracking-wider">Welcome Back</p>
              <h2 className="text-2xl md:text-3xl font-extrabold text-gray-900 tracking-tight">{profile ? profile.fullName : localStorage.getItem('username')}</h2>
              {profile && <p className="text-sm font-medium text-blue-600 mt-1 flex items-center gap-1.5"><CheckCircle2 size={16} /> KYC {profile.kycStatus}</p>}
            </div>
          </div>
          {!profile && (
            <button onClick={handleCreateProfile} className="bg-gray-900 hover:bg-black text-white px-6 py-3 rounded-full font-medium shadow-lg transition-transform hover:scale-105 active:scale-95">
              Complete Profile
            </button>
          )}
        </div>

        {/* Accounts Section */}
        {profile && (
          <div className="space-y-6">
            <div className="flex items-center justify-between px-2">
              <h3 className="text-xl md:text-2xl font-bold text-gray-900 tracking-tight">
                My Accounts
              </h3>
              <button 
                onClick={() => openAction('NEW_ACCOUNT')} 
                className="text-sm font-bold bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-full shadow-md shadow-blue-200 transition-all flex border-0 items-center gap-2 hover:-translate-y-0.5"
              >
                <Plus size={18} strokeWidth={2.5} /> <span className="hidden sm:inline">Open New Account</span><span className="sm:hidden">New</span>
              </button>
            </div>
            
            {accounts.length === 0 ? (
              <div className="bg-white border-dashed border-2 border-gray-200 rounded-[2rem] p-12 text-center text-gray-500 flex flex-col items-center justify-center">
                <CreditCard size={48} className="text-gray-300 mb-4" />
                <p className="text-lg font-medium">You don't have any accounts yet.</p>
                <p className="text-sm text-gray-400 mt-1">Open a new account to start banking securely.</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {accounts.map((acc, idx) => (
                  <div key={idx} className="bg-white rounded-[2rem] p-7 shadow-sm border border-gray-100 relative overflow-hidden group hover:shadow-lg transition-all duration-300">
                    
                    {/* Decorative Background Element */}
                    <div className="absolute -right-6 -top-6 w-32 h-32 bg-blue-50 rounded-full blur-2xl group-hover:bg-blue-100 transition-colors pointer-events-none"></div>
                    
                    {/* Card Header */}
                    <div className="flex justify-between items-start mb-6 relative z-10">
                      <div className="bg-gray-50 px-3 py-1.5 rounded-lg border border-gray-100">
                        <p className="text-xs font-semibold text-gray-500 uppercase tracking-widest">{acc.accountType} A/C</p>
                      </div>
                      <div className="w-10 h-6 rounded bg-gradient-to-r from-gray-200 to-gray-300 flex items-center justify-end px-1.5">
                        <div className="w-4 h-4 rounded-full bg-yellow-400/80"></div>
                        <div className="w-4 h-4 rounded-full bg-red-400/80 -ml-2 mix-blend-multiply"></div>
                      </div>
                    </div>

                    {/* Balance */}
                    <div className="mb-8 relative z-10">
                      <p className="text-gray-500 font-medium text-sm mb-1">Available Balance</p>
                      <h4 className="text-4xl md:text-5xl font-black text-gray-900 tracking-tighter">
                        <span className="text-gray-300 mr-1">$</span>
                        {acc.balance.toFixed(2)}
                      </h4>
                      <p className="text-gray-400 font-mono text-xs mt-2 tracking-widest flex items-center gap-2">
                        •••• •••• {acc.accountNumber.slice(-4)} 
                        <span className="bg-blue-50 text-blue-600 px-1.5 py-0.5 rounded text-[10px] font-bold hidden sm:inline-block">Acc Num: {acc.accountNumber}</span>
                      </p>
                    </div>
                    
                    {/* Action Bar (Paytm/GPay Style Circular Buttons) */}
                    <div className="flex justify-between gap-4 mt-8 pt-6 border-t border-gray-100 relative z-10">
                      <button onClick={() => openAction('DEPOSIT', acc.accountNumber)} className="flex flex-col items-center gap-2 group flex-1">
                        <div className="w-12 h-12 rounded-full bg-blue-50 text-blue-600 flex items-center justify-center group-hover:bg-blue-600 group-hover:text-white transition-colors shadow-sm">
                          <Download size={20} strokeWidth={2.5} />
                        </div>
                        <span className="text-xs font-semibold text-gray-600 group-hover:text-blue-600 transition-colors">Deposit</span>
                      </button>
                      
                      <button onClick={() => openAction('WITHDRAW', acc.accountNumber)} className="flex flex-col items-center gap-2 group flex-1">
                        <div className="w-12 h-12 rounded-full bg-purple-50 text-purple-600 flex items-center justify-center group-hover:bg-purple-600 group-hover:text-white transition-colors shadow-sm">
                          <Upload size={20} strokeWidth={2.5} />
                        </div>
                        <span className="text-xs font-semibold text-gray-600 group-hover:text-purple-600 transition-colors">Withdraw</span>
                      </button>
                      
                      <button onClick={() => openAction('TRANSFER', acc.accountNumber)} className="flex flex-col items-center gap-2 group flex-1">
                        <div className="w-12 h-12 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center group-hover:bg-emerald-600 group-hover:text-white transition-colors shadow-sm">
                          <ArrowRightLeft size={20} strokeWidth={2.5} />
                        </div>
                        <span className="text-xs font-semibold text-gray-600 group-hover:text-emerald-600 transition-colors">Transfer</span>
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </main>

      {/* Action Modal overlay */}
      {activeModal !== 'NONE' && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="bg-white rounded-[2rem] shadow-2xl w-full max-w-md overflow-hidden flex flex-col transform transition-all">
            
            {/* SUCCESS STATE */}
            {feedback?.type === 'success' ? (
              <div className="p-10 flex flex-col items-center justify-center text-center space-y-4 animate-in zoom-in duration-300">
                <div className="w-24 h-24 bg-green-100 rounded-full flex items-center justify-center relative">
                  <div className="absolute inset-0 bg-green-400 rounded-full animate-ping opacity-20"></div>
                  <CheckCircle2 className="text-green-500 w-12 h-12" strokeWidth={2.5} />
                </div>
                <h3 className="text-2xl font-black text-gray-900 tracking-tight mt-4">Successful!</h3>
                <p className="text-gray-500 font-medium">{feedback.msg}</p>
              </div>
            ) : (
              /* INPUT FORM STATE */
              <>
                <div className="flex justify-between items-center p-6 border-b border-gray-100">
                  <h3 className="text-xl font-bold text-gray-800 tracking-tight capitalize">
                    {activeModal === 'NEW_ACCOUNT' ? 'Open New Bank Account' : activeModal.toLowerCase()}
                  </h3>
                  <button onClick={closeModal} className="text-gray-400 hover:text-gray-700 bg-gray-50 hover:bg-gray-100 p-2.5 rounded-full transition-colors active:scale-95">
                    <X size={20} />
                  </button>
                </div>
                
                <form onSubmit={executeAction} className="p-6 space-y-6">
                  
                  {feedback?.type === 'error' && (
                    <div className="p-4 rounded-xl flex items-start gap-3 bg-red-50 text-red-700 border border-red-100 animate-in slide-in-from-top-2">
                      <AlertCircle className="shrink-0 mt-0.5" size={18} />
                      <p className="text-sm font-medium">{feedback.msg}</p>
                    </div>
                  )}

                  {activeModal === 'NEW_ACCOUNT' && (
                    <div className="bg-blue-50 text-blue-800 p-5 rounded-2xl border border-blue-100 text-sm font-medium leading-relaxed">
                      You are about to open a new <strong className="text-blue-900">SAVINGS</strong> account. An initial deposit of <strong className="text-blue-900">$1500.00</strong> will be automatically credited for this demo.
                    </div>
                  )}

                  {['DEPOSIT', 'WITHDRAW', 'TRANSFER'].includes(activeModal) && (
                    <div className="space-y-5">
                      <div>
                        <label className="block text-sm font-bold text-gray-700 mb-1.5 ml-1">From Account</label>
                        <input type="text" disabled value={selectedAcc} className="w-full px-4 py-3 bg-gray-100/80 border border-gray-200 rounded-2xl text-gray-500 font-mono font-medium shadow-inner" />
                      </div>
                      
                      {activeModal === 'TRANSFER' && (
                        <div className="space-y-4">
                          <div>
                            <label className="block text-sm font-bold text-gray-700 mb-1.5 ml-1">Transfer to (Self Accounts)</label>
                            <div className="flex flex-wrap gap-2">
                              {accounts.filter(a => a.accountNumber !== selectedAcc).length === 0 ? (
                                <span className="text-sm text-gray-400 italic ml-1">No other accounts</span>
                              ) : (
                                accounts.filter(a => a.accountNumber !== selectedAcc).map(a => (
                                  <button 
                                    key={a.accountNumber}
                                    type="button"
                                    onClick={() => setTargetAcc(a.accountNumber)}
                                    className={`px-4 py-2 rounded-full text-sm font-bold transition-all border ${targetAcc === a.accountNumber ? 'bg-blue-600 border-blue-600 text-white shadow-md' : 'bg-white border-gray-200 text-gray-600 hover:border-blue-400 hover:text-blue-600'}`}
                                  >
                                    ••{a.accountNumber.slice(-4)} ({a.accountType})
                                  </button>
                                ))
                              )}
                            </div>
                          </div>

                          <div>
                            <label className="block text-sm font-bold text-gray-700 mb-1.5 ml-1">Or enter manual Account Number</label>
                            <input 
                              type="text" 
                              required 
                              value={targetAcc}
                              onChange={(e) => setTargetAcc(e.target.value)}
                              placeholder="Account Number"
                              className="w-full px-4 py-3 bg-white border border-gray-300 focus:border-blue-500 focus:ring-4 focus:ring-blue-50 outline-none rounded-2xl transition-all font-mono font-medium" 
                            />
                          </div>
                        </div>
                      )}

                      <div>
                        <label className="block text-sm font-bold text-gray-700 mb-1.5 ml-1">Amount ($)</label>
                        <input 
                          type="number" 
                          required 
                          min="1" 
                          step="0.01"
                          value={amount}
                          onChange={(e) => setAmount(e.target.value)}
                          placeholder="0.00"
                          className="w-full px-4 py-3 text-lg bg-white border border-gray-300 focus:border-blue-500 focus:ring-4 focus:ring-blue-50 outline-none rounded-2xl transition-all font-bold" 
                        />
                      </div>
                    </div>
                  )}

                  <div className="flex justify-end gap-3 pt-6 border-t border-gray-100">
                    <button type="button" onClick={closeModal} className="px-6 py-3 text-gray-600 hover:bg-gray-100 font-bold rounded-xl transition-colors active:scale-95">
                      Cancel
                    </button>
                    <button 
                      type="submit" 
                      disabled={isProcessing}
                      className="px-6 py-3 bg-blue-600 hover:bg-blue-700 disabled:opacity-70 disabled:cursor-not-allowed text-white font-bold rounded-xl shadow-md hover:shadow-lg transition-all flex items-center justify-center min-w-[140px] active:scale-95"
                    >
                      {isProcessing ? (
                        <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                      ) : 'Confirm'}
                    </button>
                  </div>
                </form>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;
