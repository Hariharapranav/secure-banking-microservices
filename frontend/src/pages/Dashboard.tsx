import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';
import { User, CreditCard, LogOut, Download, Upload, ArrowRightLeft } from 'lucide-react';

const Dashboard = () => {
  const [profile, setProfile] = useState<any>(null);
  const [accounts, setAccounts] = useState<any[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const username = localStorage.getItem('username');
      // Fetch Profile
      const profileRes = await api.get(`/users/${username}`).catch(() => ({ data: null }));
      setProfile(profileRes.data);

      if (profileRes.data) {
        // Fetch accounts
        const accountsRes = await api.get(`/accounts/user/${username}`).catch(() => ({ data: [] }));
        setAccounts(accountsRes.data);
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

  const handleCreateAccount = async () => {
    const username = localStorage.getItem('username');
    try {
      await api.post('/accounts', {
        username,
        accountType: 'SAVINGS',
        initialDeposit: 1500.0, // Backend requires >= 1000
        branchCode: 'MAIN01',
        ifscCode: 'BANK0000001'
      });
      fetchData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Error creating account');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col w-full text-gray-800">
      <nav className="bg-white border-b border-gray-200 px-6 py-4 shadow-sm flex items-center justify-between">
        <h1 className="text-2xl font-black text-blue-600">SecureBank</h1>
        <button onClick={handleLogout} className="flex flex-row items-center gap-2 text-gray-500 hover:text-red-500 font-medium transition-colors">
          <LogOut size={20} /> Logout
        </button>
      </nav>

      <main className="flex-1 max-w-6xl w-full mx-auto p-6 md:p-10 space-y-8">
        
        {/* User Profile Section */}
        <div className="bg-white rounded-2xl shadow p-6 border border-gray-100 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="bg-blue-100 p-4 rounded-full text-blue-600">
              <User size={32} />
            </div>
            <div>
              <h2 className="text-2xl font-bold">{profile ? profile.fullName : 'Welcome, ' + localStorage.getItem('username')}</h2>
              <p className="text-gray-500">{profile ? `KYC Status: ${profile.kycStatus}` : 'Complete your profile to unlock banking'}</p>
            </div>
          </div>
          {!profile && (
            <button onClick={handleCreateProfile} className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg font-medium shadow-md transition-all">
              Complete Profile
            </button>
          )}
        </div>

        {/* Accounts Section */}
        {profile && (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-xl font-bold flex items-center gap-2">
                <CreditCard className="text-blue-500" /> Your Accounts
              </h3>
              <button onClick={handleCreateAccount} className="text-sm font-semibold bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded-lg shadow-sm transition-all">
                + Open New Account
              </button>
            </div>
            
            {accounts.length === 0 ? (
              <div className="bg-white border-dashed border-2 border-gray-200 rounded-2xl p-10 text-center text-gray-500">
                You don't have any accounts yet.
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {accounts.map((acc, idx) => (
                  <div key={idx} className="bg-gradient-to-br from-blue-600 to-blue-800 rounded-2xl p-6 text-white shadow-xl relative overflow-hidden group">
                    <div className="absolute -right-10 -top-10 w-40 h-40 bg-white opacity-10 rounded-full blur-2xl group-hover:bg-blue-300 transition-colors"></div>
                    <p className="text-blue-200 font-medium mb-1">Account Number</p>
                    <h4 className="text-xl font-mono tracking-widest mb-6">{acc.accountNumber}</h4>
                    
                    <p className="text-blue-200 font-medium mb-1">Available Balance</p>
                    <p className="text-4xl font-bold tracking-tight">${acc.balance.toFixed(2)}</p>
                    
                    <div className="mt-6 flex justify-between gap-3 bg-white/10 rounded-xl p-3 backdrop-blur-md">
                      <button className="flex-1 flex flex-col items-center gap-1 hover:text-green-300 transition-colors">
                        <Download size={20} /> <span className="text-xs">Deposit</span>
                      </button>
                      <button className="flex-1 flex flex-col items-center gap-1 hover:text-red-300 transition-colors">
                        <Upload size={20} /> <span className="text-xs">Withdraw</span>
                      </button>
                      <button className="flex-1 flex flex-col items-center gap-1 hover:text-yellow-300 transition-colors">
                        <ArrowRightLeft size={20} /> <span className="text-xs">Transfer</span>
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
};

export default Dashboard;
