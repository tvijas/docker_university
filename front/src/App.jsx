import React from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import Registration from './components/Registration';
import Login from './components/Login';
import ResetPassword from './components/ResetPassword';
import EmailVerification from './components/EmailVerification';
import ToDoList from './components/ToDoList';
import './App.css'
import ResetPasswordVerification from './components/ResetPasswordVerification';

const App = () => {
  return (
    <Router>
      <div className="App">
        <Routes>

          <Route path="/register" element={<Registration />} />
          <Route path="/login" element={<Login />} />
          <Route path="/reset-password" element={<ResetPassword />} />
          <Route path="/todo" element={<ToDoList />} />
          <Route path="/" element={<Navigate replace to="/login" />} />
          <Route path="/user/verify/local" element={<EmailVerification />} />
          <Route path="/check-email" element={<EmailVerification />} />
          <Route path="/summit-password-change" element={<ResetPasswordVerification />} />
          <Route path="/user/summit-password-change" element={<ResetPasswordVerification />} />

        </Routes>
      </div>
    </Router>
  );
};

export default App;