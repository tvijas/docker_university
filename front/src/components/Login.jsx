import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import baseRequest from '../network/baseRequest';
import "./styles/Login.css";

const Login = () => {
  const [formData, setFormData] = useState({
    login: '',
    password: ''
  });
  const [errors, setErrors] = useState({});
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const validateForm = () => {
    const newErrors = {};
    if (!formData.login.trim()) {
      newErrors.login = 'Login can\'t be empty';
    }
    if (!formData.password.trim()) {
      newErrors.password = 'Password can\'t be empty';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (validateForm()) {
      try {
        const response = await baseRequest.post('/api/user/login', formData, {
          headers: {
            'Content-Type': 'application/json',
          },
        });
        
        console.log("Status code: " + response.status)
        if (response.status === 200) {
          navigate('/todo');
        }
      } catch (error) {
        if (error.response && error.response.data) {
          console.log(error)
          setErrors({ general: 'Login or password is incorrect' });
        } else {
          console.error('Login error:', error);
          setErrors({ general: 'An error occurred. Please try again.' });
        }
      }
    }
  };

  return (
    <div id="log">
      <h2>Login</h2>
      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="login" id='log1'>Login:</label>
          <input
            type="text"
            id="login"
            name="login"
            className="txt1"
            value={formData.login}
            onChange={handleChange}
          />
          
          {errors.login && <div className="error">{errors.login}</div>}
        </div>
        <div>
          <label htmlFor="password">Password:</label>
          <input
            type="password"
            id="password"
            name="password"
            className="txt1"
            value={formData.password}
            onChange={handleChange}
          />
          
          {errors.password && <div className="error">{errors.password}</div>}
        </div>
        {errors.general && <div className="error">{errors.general}</div>}
        <button type="submit" id='sub'>Login</button>
      </form>
      <p>
        You dont have an account? <Link to="/register">Sign up</Link>
      </p>
      <p>
        <Link to="/reset-password">Reset password</Link>
      </p>
    </div>
  );
};

export default Login;