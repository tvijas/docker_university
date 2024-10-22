import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import baseRequest from '../network/baseRequest';
const ResetPasswordVerification = () => {
  const [message, setMessage] = useState('Please, check your email for submission link.');
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const queryParams = new URLSearchParams(location.search);
    const code = queryParams.get('code');
    const email = queryParams.get('email');

    if (code && email) {
      verifyEmail(code, email);
    }
  }, [location]);

  const verifyEmail = async (code, email) => {
    try {
      const response = await baseRequest.post(`/api/user/summit-password-change?code=${code}&email=${email}`);
      if (response.status === 200) {
        setMessage('Password successfully changed. Redirecting to login page...');
        setTimeout(() => navigate('/login'), 3000);
      } else{
        setMessage('Code is expired. Try again')
      }
    } catch (error) {
      setMessage('Error occured. Try again');
    }
  };

  return (
    <div>
      <h2>Change password verification</h2>
      <p>{message}</p>
    </div>
  );
};

export default ResetPasswordVerification;