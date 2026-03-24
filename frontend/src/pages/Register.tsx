import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api';
import { Mail, Lock, User, UserPlus } from 'lucide-react';

const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    fullName: '',
    phone: '',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/auth/register', formData);
      setSuccess('Account created successfully! Redirecting...');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center relative overflow-hidden font-sans">
      {/* Abstract Background Shapes */}
      <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-blue-400/20 rounded-full blur-3xl pointer-events-none"></div>
      <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-indigo-400/20 rounded-full blur-3xl pointer-events-none"></div>

      <div className="w-full max-w-md p-8 md:p-10 space-y-7 bg-white/80 backdrop-blur-xl border border-white/50 rounded-[2.5rem] shadow-2xl relative z-10 m-4">
        <div className="text-center">
          <div className="w-16 h-16 bg-gradient-to-br from-blue-600 to-indigo-600 rounded-2xl mx-auto mb-6 flex items-center justify-center shadow-lg shadow-blue-200">
            <UserPlus className="text-white w-8 h-8 ml-1" />
          </div>
          <h2 className="text-3xl md:text-4xl font-extrabold text-gray-900 mb-2 tracking-tight">Join Us</h2>
          <p className="text-gray-500 font-medium">Create your secure bank account</p>
        </div>

        {error && <div className="p-4 bg-red-50 text-red-600 text-sm rounded-xl text-center font-medium border border-red-100 flex items-center justify-center gap-2">
          <span className="w-1.5 h-1.5 rounded-full bg-red-500"></span> {error}
        </div>}
        {success && <div className="p-4 bg-green-50 text-green-600 text-sm rounded-xl text-center font-medium border border-green-100 flex items-center justify-center gap-2">
          <span className="w-1.5 h-1.5 rounded-full bg-green-500"></span> {success}
        </div>}

        <form onSubmit={handleRegister} className="space-y-4">
          <div className="relative group">
            <User className="absolute left-4 top-3.5 h-5 w-5 text-gray-400 group-focus-within:text-blue-500 transition-colors" />
            <input type="text" placeholder="Full Name" required
              className="w-full pl-12 pr-4 py-3.5 bg-white/50 border border-gray-200 rounded-2xl focus:ring-4 focus:ring-blue-50 focus:border-blue-500 outline-none transition-all font-medium text-gray-700"
              value={formData.fullName} onChange={(e) => setFormData({...formData, fullName: e.target.value})}
            />
          </div>
          <div className="relative group">
            <User className="absolute left-4 top-3.5 h-5 w-5 text-gray-400 group-focus-within:text-blue-500 transition-colors" />
            <input type="text" placeholder="Username" required
              className="w-full pl-12 pr-4 py-3.5 bg-white/50 border border-gray-200 rounded-2xl focus:ring-4 focus:ring-blue-50 focus:border-blue-500 outline-none transition-all font-medium text-gray-700"
              value={formData.username} onChange={(e) => setFormData({...formData, username: e.target.value})}
            />
          </div>
          <div className="relative group">
            <Mail className="absolute left-4 top-3.5 h-5 w-5 text-gray-400 group-focus-within:text-blue-500 transition-colors" />
            <input type="email" placeholder="Email Address" required
              className="w-full pl-12 pr-4 py-3.5 bg-white/50 border border-gray-200 rounded-2xl focus:ring-4 focus:ring-blue-50 focus:border-blue-500 outline-none transition-all font-medium text-gray-700"
              value={formData.email} onChange={(e) => setFormData({...formData, email: e.target.value})}
            />
          </div>
          <div className="relative group">
            <Lock className="absolute left-4 top-3.5 h-5 w-5 text-gray-400 group-focus-within:text-blue-500 transition-colors" />
            <input type="password" placeholder="Password" required
              className="w-full pl-12 pr-4 py-3.5 bg-white/50 border border-gray-200 rounded-2xl focus:ring-4 focus:ring-blue-50 focus:border-blue-500 outline-none transition-all font-medium text-gray-700"
              value={formData.password} onChange={(e) => setFormData({...formData, password: e.target.value})}
            />
          </div>

          <button type="submit" className="w-full flex items-center justify-center gap-2 py-4 px-4 bg-gray-900 hover:bg-black text-white font-semibold rounded-2xl shadow-lg hover:shadow-xl transition-all active:scale-95 mt-2">
            Sign Up
          </button>
        </form>

        <p className="text-center text-sm text-gray-500 font-medium">
          Already have an account? <Link to="/login" className="font-bold text-blue-600 hover:text-blue-700 hover:underline transition-colors">Sign in</Link>
        </p>
      </div>
    </div>
  );
};

export default Register;
